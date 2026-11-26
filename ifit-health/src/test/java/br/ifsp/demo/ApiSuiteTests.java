package br.ifsp.demo;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("br.ifsp.demo.integration.api")
@IncludeTags("ApiTest")
@SuiteDisplayName("Todos os testes de integração da API REST")
public class ApiSuiteTests {
}
