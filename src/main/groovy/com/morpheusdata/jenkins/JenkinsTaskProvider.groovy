package com.morpheusdata.jenkins

import com.morpheusdata.core.ExecutableTaskInterface
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.providers.TaskProvider
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.Icon
import com.morpheusdata.model.Instance
import com.morpheusdata.model.OptionType
import com.morpheusdata.model.Task
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.TaskResult
import com.morpheusdata.model.TaskType
import com.morpheusdata.model.Workload

class JenkinsTaskProvider implements TaskProvider {
    MorpheusContext morpheusContext
    Plugin plugin
    JenkinsTaskService service

    JenkinsTaskProvider(Plugin plugin, MorpheusContext morpheusContext) {
        this.plugin = plugin
        this.morpheusContext = morpheusContext
        this.service = new JenkinsTaskService(morpheusContext)
    }

    @Override
    TaskType.TaskScope getScope() {
        return TaskType.TaskScope.app
    }

    @Override
    String getDescription() {
        return "Allows the triggering of a Jenkins Pipeline Build and waits for task to complete"
    }

    /**
     * A flag indicating if this task can be configured to execute on a local context
     * @return boolean
     */
    @Override
    Boolean isAllowExecuteLocal() {
        return true
    }

    /**
     * A flag indicating if this task can be configured to execute on a remote context
     * @return boolean
     */
    @Override
    Boolean isAllowExecuteRemote() {
        return false
    }

    /**
     * A flag indicating if this task can be configured to execute on a resource
     * @return boolean
     */
    @Override
    Boolean isAllowExecuteResource() {
        return false
    }

    /**
     * A flag indicating if this task can be configured to execute a script from a git repository
     * @return boolean
     */
    @Override
    Boolean isAllowLocalRepo() {
        return false
    }

    /**
     * A flag indicating if this task can be configured with ssh keys
     * @return boolean
     */
    @Override
    Boolean isAllowRemoteKeyAuth() {
        return false
    }

    /**
     * A flag indicating if the TaskType presents results that can be chained into other tasks
     * @return boolean
     */
    @Override
    Boolean hasResults() {
        return true
    }

    /**
     * Additional task configuration
     * {@link OptionType}
     * @return a List of OptionType
     */
    @Override
    List<OptionType> getOptionTypes() {
        return [
                new OptionType(code: 'jenkins.serviceUrl', name: 'Service URL', inputType: OptionType.InputType.TEXT, fieldName: 'serviceUrl', fieldLabel: 'API Url', displayOrder: 0),
                new OptionType(code: 'jenkins.serviceUser', name: 'Service Username', inputType: OptionType.InputType.TEXT, fieldName: 'serviceUsername', fieldLabel: 'Username', displayOrder: 2),
                new OptionType(code: 'jenkins.serviceToken', name: 'Service Token', inputType: OptionType.InputType.PASSWORD, fieldName: 'servicePassword', fieldLabel: 'Token', displayOrder: 3),
                new OptionType(code: 'jenkins.jobName', name: 'Job Name', inputType: OptionType.InputType.TEXT, fieldName: 'jobName', fieldLabel: 'Job Name', displayOrder: 4),
                new OptionType(code: 'jenkins.buildParameters', name: 'Build Parameters', inputType: OptionType.InputType.CODE_EDITOR, fieldName: 'buildParameters', fieldLabel: 'Build Parameters', displayOrder: 5),
        ]
    }

    /**
     * Returns the Morpheus Context for interacting with data stored in the Main Morpheus Application
     *
     * @return an implementation of the MorpheusContext for running Future based rxJava queries
     */
    @Override
    MorpheusContext getMorpheus() {
        return morpheusContext
    }

    /**
     * Returns the instance of the Plugin class that this provider is loaded from
     * @return Plugin class contains references to other providers
     */
    @Override
    Plugin getPlugin() {
        return plugin
    }

    /**
     * A unique shortcode used for referencing the provided provider. Make sure this is going to be unique as any data
     * that is seeded or generated related to this provider will reference it by this code.
     * @return short code string that should be unique across all other plugin implementations.
     */
    @Override
    String getCode() {
        return 'jenkins'
    }

    /**
     * Provides the provider name for reference when adding to the Morpheus Orchestrator
     * NOTE: This may be useful to set as an i18n key for UI reference and localization support.
     *
     * @return either an English name of a Provider or an i18n based key that can be scanned for in a properties file.
     */
    @Override
    String getName() {
        return 'Jenkins Trigger Build'
    }

    /**
     * Returns the Task Type Icon for display when a user is browsing tasks
     * @return Icon representation of assets stored in the src/assets of the project.
     */
    @Override
    Icon getIcon() {
        return new Icon(path:"jenkins-black.svg", darkPath: "jenkins-white.svg")
    }

    /**
     * @deprecated Method has been rolled up into the TaskProvider interface directly.
     */
    @Override
    ExecutableTaskInterface getService() {
        return null
    }

    /**
     * Task execution in a local context
     *
     * @param task Morpheus task to be executed
     * @param opts contains the values of any OptionType that were defined for this task
     * @param workload optional Workload details
     * @param server optional ComputeServer details
     * @param instance optional Instance details
     * @return the result of the task
     */
    @Override
    TaskResult executeLocalTask(Task task, Map opts, Workload workload, ComputeServer server, Instance instance) {
        TaskConfig config
        if(workload) {
            config = morpheus.buildWorkloadConfig(workload, [:], task, [], opts).blockingGet()
        } else if(instance) {
            config = morpheus.buildInstanceConfig(instance, [:], task, [], opts).blockingGet()
        } else {
            config = morpheus.buildComputeServerConfig(server, [:], task, [], opts).blockingGet()
        }
        service.executeTask(task, config)
    }

    /**
     * Task execution on a provisioned ComputeServer
     *
     * @param server server details
     * @param task Morpheus task to be executed
     * @param opts contains the values of any OptionType that were defined for this task
     * @return the result of the task
     */
    @Override
    TaskResult executeServerTask(ComputeServer server, Task task, Map opts) {
        TaskConfig config = morpheus.buildComputeServerConfig(server, [:], task, [], opts).blockingGet()
        service.executeTask(task, config)
    }

    @Override
    TaskResult executeServerTask(ComputeServer server, Task task) {
        return executeServerTask(server, task, [:])
    }

    /**
     * Task execution on a provisioned Workload
     *
     * @param workload Workload details
     * @param task Morpheus task to be executed
     * @param opts contains the values of any OptionType that were defined for this task
     * @return the result of the task
     */
    @Override
    TaskResult executeContainerTask(Workload workload, Task task, Map opts) {
        TaskConfig config = morpheus.buildWorkloadConfig(workload, [:], task, [], opts).blockingGet()
        service.executeTask(task, config)
    }

    @Override
    TaskResult executeContainerTask(Workload workload, Task task) {
        return executeContainerTask(workload, task, [:])
    }

    /**
     * Task execution in a remote context
     *
     * @param task Morpheus task to be executed
     * @param opts contains the values of any OptionType that were defined for this task
     * @param workload optional {@link Workload} details
     * @param server optional {@link ComputeServer} details
     * @param instance optional {@link Instance} details
     * @return the result of the task
     */
    @Override
    TaskResult executeRemoteTask(Task task, Map opts, Workload workload, ComputeServer server, Instance instance) {
        TaskConfig config = morpheus.buildComputeServerConfig(server, [:], task, [], opts).blockingGet()
        service.executeTask(task, config)
    }

    /**
     * Task execution in a remote context
     *
     * @param task Morpheus task to be executed
     * @param workload optional {@link Workload} details
     * @param server optional {@link ComputeServer} details
     * @param instance optional {@link Instance} details
     * @return the result of the task
     */
    @Override
    TaskResult executeRemoteTask(Task task, Workload workload, ComputeServer server, Instance instance) {
        return executeRemoteTask(task, [:], workload, server, instance)
    }
}
