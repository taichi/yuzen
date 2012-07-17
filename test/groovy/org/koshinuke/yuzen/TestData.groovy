package org.koshinuke.yuzen

/**
 * @author taichi
 */
class TestData {

	static Properties props

	static {
		ClassLoader cl = TestData.class.classLoader
		URL url = cl.getResource('test.properties');
		props = new Properties()
		if(url == null) {
			System.err.println("test.properties doesn't exist");
		} else {
			url.withInputStream { props.load(it) }
		}

		props.each {
			if(it.key.startsWith('systemProp')) {
				def s = it.key.replace('systemProp.','')
				System.properties[s] = it.value
			}
		}
	}

	public static void overwrite(dest) {
		props.each {
			dest[it.key] = it.value
		}
	}
}
