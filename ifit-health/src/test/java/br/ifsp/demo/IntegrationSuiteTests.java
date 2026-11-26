package br.ifsp.demo;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("br.ifsp.demo.integration")
@IncludeTags("IntegrationTest")
@SuiteDisplayName("Todos os testes de integração (API e persistência)")
public class IntegrationSuiteTests {
}
