package org.torquebox.interp.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.util.ByteList;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.metadata.PoolingMetaData;
import org.yaml.snakeyaml.Yaml;

public class PoolingYamlParsingDeployer extends AbstractVFSParsingDeployer<PoolingMetaData> {

	public PoolingYamlParsingDeployer() {
		super(PoolingMetaData.class);
		setName("pooling.yml");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected PoolingMetaData parse(VFSDeploymentUnit unit, VirtualFile file, PoolingMetaData root) throws Exception {
		Yaml yaml = new Yaml();
		Map<String,Object> pooling = (Map<String, Object>) yaml.load(file.openStream());

		PoolingMetaData poolingMetaData = new PoolingMetaData();

		for (String name : pooling.keySet()) {

			Object pool = pooling.get(name);

			PoolMetaData poolMetaData = new PoolMetaData();
			poolMetaData.setName(name.toString());

			if (pool instanceof Map) {
				Map<ByteList, Object> poolMap = (Map<ByteList, Object>) pool;

				ByteList key = null;

				key = ByteList.create("min");
				if (poolMap.get(key) != null) {
					poolMetaData.setMinimumSize(((Number) poolMap.get(key)).intValue());
				}

				key = ByteList.create("max");
				if (poolMap.get(key) != null) {
					poolMetaData.setMaximumSize(((Number) poolMap.get(key)).intValue());
				}
			} else if (pool instanceof ByteList) {
				if ( pool.toString().equals( "shared" ) ) {
					poolMetaData.setShared();
				} else if ( pool.toString().equals( "global" ) ) {
					poolMetaData.setGlobal();
				}
				log.debug("pool data is " + pool.getClass() + " -- " + pool);
			}

			poolingMetaData.addPool(poolMetaData);
			log.debug("configured pool " + poolMetaData);
		}

		return poolingMetaData;
	}

}