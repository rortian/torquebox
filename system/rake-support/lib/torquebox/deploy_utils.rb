# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'tmpdir'
require 'rbconfig'
require 'yaml'
require 'rake'


module TorqueBox
  module DeployUtils
    class << self

      def jboss_home
        jboss_home = File.expand_path(ENV['JBOSS_HOME']) if ENV['JBOSS_HOME']
        jboss_home ||= File.join(File.expand_path(ENV['TORQUEBOX_HOME']), "jboss") if ENV['TORQUEBOX_HOME']
        raise "$JBOSS_HOME is not set" unless jboss_home
        return jboss_home
      end

      def torquebox_home
        torquebox_home = nil
        if ( ENV['TORQUEBOX_HOME'] )
          torquebox_home = File.expand_path(ENV['TORQUEBOX_HOME'])
        end
        torquebox_home
      end

      def jboss_conf
        ENV['TORQUEBOX_CONF'] || ENV['JBOSS_CONF'] || 'default'
      end

      def server_dir
        File.join("#{jboss_home}","server", "#{jboss_conf}" )
      end

      def config_dir
        File.join("#{server_dir}","conf")
      end

      def properties_dir
        File.join("#{config_dir}","props")
      end

      def deploy_dir
        d = File.join( torquebox_home, 'apps' )
        if ( File.exists?( d ) && File.directory?( d ) )
          return d
        end

        File.join( "#{server_dir}", "deploy" )
      end

      def deployers_dir
        File.join("#{server_dir}","deployers")
      end

      def archive_name(root=Dir.pwd)
        File.basename( root ) + '.knob'
      end

      def deployment_name(root = Dir.pwd)
        File.basename( root ) + '-knob.yml'
      end

      def check_server
        matching = Dir[ "#{deployers_dir}/torquebox*deployer*" ]
        raise "No TorqueBox deployer installed in #{deployers_dir}" if ( matching.empty? )
      end

      def run_command_line
        cmd = Config::CONFIG['host_os'] =~ /mswin/ ? "bin\\run" : "/bin/sh bin/run.sh"
        options = ENV['JBOSS_OPTS']
        cmd += " -b 0.0.0.0" unless /((^|\s)-b\s|(^|\s)--host=)/ =~ options
        "#{cmd} -c #{jboss_conf} #{options}"
      end

      def run_server
        Dir.chdir(jboss_home) do
          old_trap = trap("INT") do
            puts "caught SIGINT, shutting down"
          end
          exec_command(run_command_line)
          trap("INT", old_trap )
        end
      end


      def create_archive(archive = archive_name, app_dir = Dir.pwd, dest_dir = Dir.pwd)
        skip_files = %w{ ^log$ ^tmp$ ^test$ ^spec$ \.knob$ vendor }

        archive_path = File.join(dest_dir, archive)
        
        Dir.chdir( app_dir ) do
          include_files = []
          Dir[ "*", ".bundle" ].each do |entry|
            entry = File.basename( entry )
            unless ( skip_files.any?{ |regex| entry.match(regex)} )
              include_files << entry
            end
          end

          Dir[ 'vendor/*' ].each do |entry|
            include_files << entry unless ( entry == 'vendor/cache' )
          end

          cmd = "jar cvf #{archive_path} #{include_files.join(' ')}"
          exec_command( cmd )
        end

        archive_path
      end

      def freeze_gems(app_dir = Dir.pwd)
        Dir.chdir( app_dir ) do
          jruby = File.join( RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'] )
          jruby << " --1.9" if RUBY_VERSION =~ /^1\.9\./
          exec_command( "#{jruby} -S bundle package" )
          exec_command( "#{jruby} -S bundle install --local --path vendor/bundle" )
        end
      end

      def basic_deployment_descriptor(options = {})
        env = options[:env]
        env ||= defined?(RACK_ENV) ? RACK_ENV : ENV['RACK_ENV']
        env ||= defined?(::Rails) ? ::Rails.env : ENV['RAILS_ENV']

        root = options[:root] || Dir.pwd
        context_path = options[:context_path]
        
        d = {}
        d['application'] = {}
        d['application']['root'] = root
        d['application']['env'] = env.to_s if env

        if !context_path &&
            !(File.exists?( File.join( root, "torquebox.yml" )) ||
              File.exists?( File.join( root, "config", "torquebox.yml" ) ))
          context_path = '/'
        end

        if context_path
          d['web'] = {}
          d['web']['context'] = context_path
        end

        d
      end

      def deploy_yaml(deployment_descriptor, name = deployment_name, dest_dir = deploy_dir)
        deployment = File.join( dest_dir, name )
        File.open( deployment, 'w' ) do |file|
          YAML.dump( deployment_descriptor, file )
        end
        [name, deploy_dir]
      end

      def deploy_archive(archive_path = nil, dest_dir = deploy_dir)
        archive_path ||= File.join( Dir.pwd, archive_name )
        FileUtils.cp( archive_path, dest_dir )
        [File.basename( archive_path ), dest_dir]
      end

      def undeploy(name = deployment_name, from_dir = deploy_dir)
        deployment = File.join( from_dir, name )
        if File.exists?( deployment )
          FileUtils.rm_rf( deployment )
          [name, from_dir]
        else
          nil
        end
      end

      def append_credentials(user_data)
        properties_file = "#{properties_dir}/torquebox-users.properties"
        File.open( properties_file, 'a' ) do |file|
          user_data.each do |data|
            file.puts( data.join( '=' ) )
          end
        end
        properties_file
      end
      
      def exec_command(cmd)
        IO.popen4( cmd ) do |pid, stdin, stdout, stderr|
          stdin.close
          [
           Thread.new(stdout) {|stdout_io|
             stdout_io.each_line do |l|
               STDOUT.puts l
               STDOUT.flush
             end
             stdout_io.close
           },

           Thread.new(stderr) {|stderr_io|
             stderr_io.each_line do |l|
               STDERR.puts l
               STDERR.flush
             end
           }
          ].each( &:join )
        end

      end


    end
  end
end
