package org.bahmni.reports.dao.impl;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.*;

public class GenericObservationDaoImpl implements GenericDao {

    private BahmniReportsProperties bahmniReportsProperties;
    private Report<GenericObservationReportConfig> report;

    public GenericObservationDaoImpl(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        this.report = report;
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public ResultSet getResultSet(Connection connection,
                                  String startDate, String endDate, List<String> conceptNamesToFilter)
            throws SQLException {
        String sql;
        if (report.getConfig() != null && report.getConfig().isEncounterPerRow()) {
            sql = getFileContent("sql/genericObservationReportInOneRow.sql");
        } else {
            sql = getFileContent("sql/genericObservationReport.sql");
        }
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (report.getConfig() != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(report.getConfig()));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("locationTagsToFilter", constructLocationTagsToFilter(report.getConfig()));
            sqlTemplate.add("conceptClassesToFilter", constructConceptClassesToFilter(report.getConfig()));
            sqlTemplate.add("programsToFilter", constructProgramsString(report.getConfig()));
            sqlTemplate.add("conceptNamesToFilter", constructConceptNamesToFilter(report, bahmniReportsProperties));
            sqlTemplate.add("selectConceptNamesSql", constructConceptNameSelectSqlIfShowInOneRow(conceptNamesToFilter, report.getConfig()));
            sqlTemplate.add("showProvider", report.getConfig().showProvider());
            sqlTemplate.add("visitTypesToFilter", constructVisitTypesString(getVisitTypesToFilter(report.getConfig())));
        }
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(report.getConfig()));

        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }

}