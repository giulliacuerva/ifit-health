package br.ifsp.demo;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("br.ifsp.demo.integration.persistence")
@IncludeTags("PersistenceTest")
@SuiteDisplayName("Todos os testes de integração com o banco")
public class PersistenceSuiteTests {
}
