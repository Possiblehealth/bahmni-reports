package org.bahmni.reports.report.integrationtests;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertEquals;

public class ConcatenatedReportTest extends BaseIntegrationTest {
    public ConcatenatedReportTest() {
        super("src/test/resources/config/concatenatedReportConfig.json");
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("datasets/genericObservationReportDataSet.xml");
        executeDataSet("datasets/genericVisitReportDataSet.xml");
    }

    @Test
    public void shouldConcatenateReports() throws Exception {
        XSSFWorkbook report = fetchXlsReport("Concatenated Report Name", "2014-04-01", "2016-08-30");

        assertEquals(2, report.getNumberOfSheets());
        assertEquals("Observation report", report.getSheetAt(0).getSheetName());
        assertEquals("Generic Visit Report", report.getSheetAt(1).getSheetName());
    }

    @Test
    public void shouldThrowExceptionForConcatenatedReportOfCsvResponseType() throws Exception {
        MvcResult mvcResult = fetchMvcResult("Concatenated Report Name", "2014-04-01", "2016-08-30", "text/csv", true);
        String response = mvcResult.getResponse().getContentAsString();
        String expectedResponse = "<h2>Incorrect Configuration</h2><h3>CSV format is not supported for Concatenated report</h3>";
        assertEquals(expectedResponse, response);
    }
}