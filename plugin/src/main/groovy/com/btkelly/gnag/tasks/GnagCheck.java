/**
 * Copyright 2016 Bryan Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.btkelly.gnag.tasks;

import com.btkelly.gnag.GnagPluginExtension;
import com.btkelly.gnag.reporters.*;
import com.btkelly.gnag.utils.GnagReportBuilder;
import com.btkelly.gnag.utils.ReportHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bobbake4 on 4/1/16.
 */
public class GnagCheck extends DefaultTask {

    public static final String TASK_NAME = "gnagCheck";

    public static void addTask(Project project, GnagPluginExtension gnagPluginExtension) {
        Map<String, Object> taskOptions = new HashMap<>();

        taskOptions.put(Task.TASK_NAME, TASK_NAME);
        taskOptions.put(Task.TASK_TYPE, GnagCheck.class);
        taskOptions.put(Task.TASK_GROUP, "Verification");
        taskOptions.put(Task.TASK_DEPENDS_ON, "check");
        taskOptions.put(Task.TASK_DESCRIPTION, "Runs Gnag checks and generates an HTML report");

        GnagCheck gnagCheckTask = (GnagCheck) project.task(taskOptions, TASK_NAME);
        gnagCheckTask.setGnagPluginExtension(gnagPluginExtension);
        gnagCheckTask.reporters.add(new CheckstyleReporter(gnagPluginExtension.checkstyle, project));
        gnagCheckTask.reporters.add(new PMDReporter(gnagPluginExtension.pmd, project));
        gnagCheckTask.reporters.add(new FindbugsReporter(gnagPluginExtension.findbugs, project));
        gnagCheckTask.reporters.add(new AndroidLintReporter(gnagPluginExtension.androidLint, project));
    }

    private final List<Reporter> reporters = new ArrayList<>();
    private GnagPluginExtension gnagPluginExtension;

    @TaskAction
    public void taskAction() {
        if (gnagPluginExtension.isEnabled()) {
            executeGnagCheck();
        }
    }

    private void executeGnagCheck() {
        boolean foundErrors = false;

        ReportHelper reportHelper = new ReportHelper(getProject());
        GnagReportBuilder gnagReportBuilder = new GnagReportBuilder(getProject(), reportHelper.getReportsDir());

        for (Reporter reporter : reporters) {

            if (reporter.isEnabled()) {

                if (reporter instanceof BaseExecutedReporter) {
                    ((BaseExecutedReporter) reporter).executeReporter();
                }

                if (reporter.hasErrors()) {
                    foundErrors = true;
                    reporter.appendReport(gnagReportBuilder);
                }
            }
        }

        gnagReportBuilder.writeFile();

        if (foundErrors && gnagPluginExtension.shouldFailOnError()) {
            throw new GradleException("One or more reporters has caused the build to fail");
        }
    }

    private void setGnagPluginExtension(GnagPluginExtension gnagPluginExtension) {
        this.gnagPluginExtension = gnagPluginExtension;
    }
}
