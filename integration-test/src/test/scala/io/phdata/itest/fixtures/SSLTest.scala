package io.phdata.itest.fixtures

trait SSLTest {

  System.setProperty("javax.net.ssl.trustStore", "itest-config/integration-test.jks")
}
