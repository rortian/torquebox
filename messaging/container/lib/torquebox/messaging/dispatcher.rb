
module TorqueBox
  module Messaging
    module Dispatcher
      def self.dispatch(listener_class_name, listener_class_location, session, message)
        unless ( listener_class_location.nil? )
          load listener_class_location
        end
        listener_class = eval listener_class_name
        listener = listener_class.new
        listener.session = session if ( listener.respond_to?( "session=" ) )
        message_type = message.get_string_property( 'torqueboxMessageType')
        if ( message_type == 'object' && listener.respond_to?( :on_object ) )
          encoded = message.text
          serialized = Base64.decode64( encoded )
          object = Marshal.restore( serialized )
          method = listener.method( :on_object )  
          if ( method.arity == 2 ) 
            method.call( object, message )
          else
            method.call( object )
          end
        elsif ( message_type == 'text' && listener.respond_to?( :on_text ) )
          object = message.text
          method = listener.method( :on_text )  
          if ( method.arity == 2 ) 
            method.call( object, message )
          else
            method.call( object )
          end
        else
          listener.on_message( message )
        end
      end 
    end
  end
end