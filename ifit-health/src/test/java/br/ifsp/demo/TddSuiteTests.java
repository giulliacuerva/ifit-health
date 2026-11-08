package br.ifsp.demo;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("br.ifsp.demo.domain")
@IncludeTags("TDD")
@SuiteDisplayName("All tests with TDD")
public class TddSuiteTests {
}
