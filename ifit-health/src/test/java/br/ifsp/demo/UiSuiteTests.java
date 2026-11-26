package br.ifsp.demo;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("br.ifsp.demo.ui")
@IncludeTags("UiTest")
@SuiteDisplayName("Todos os testes de UI")
public class UiSuiteTests {
}
