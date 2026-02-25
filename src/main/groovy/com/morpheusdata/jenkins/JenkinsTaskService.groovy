package com.morpheusdata.jenkins

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.model.Task
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.TaskResult
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class JenkinsTaskService {
    MorpheusContext morpheus

    JenkinsTaskService(MorpheusContext morpheus) {
        this.morpheus = morpheus
    }

    /**
     * Core Jenkins build trigger and polling logic
     *
     * @param task
     * @param config
     * @return data and output are the reversed text
     */
    TaskResult executeTask(Task task, TaskConfig config) {
        println config.accountId
        String jenkinsUrl = task.taskOptions.find { it.optionType.code == 'jenkins.serviceUrl' }?.value
        String jenkinsUser = task.taskOptions.find { it.optionType.code == 'jenkins.serviceUser' }?.value
        String jenkinsToken = task.taskOptions.find { it.optionType.code == 'jenkins.serviceToken' }?.value
        String jobName = task.taskOptions.find { it.optionType.code == 'jenkins.jobName' }?.value
        String jenkinsParameters = task.taskOptions.find { it.optionType.code == 'jenkins.buildParameters' }?.value

        HttpApiClient client = new HttpApiClient()
        try {
            String path = "/job/${jobName}/${jenkinsParameters ? 'buildWithParameters' : 'build'}"
            HttpApiClient.RequestOptions requestOptions = new HttpApiClient.RequestOptions()
            requestOptions.headers = ['Accept':'application/json']
            def parametersMap = [:]
            if (jenkinsParameters) {
              parametersMap = new JsonSlurper().parseText(jenkinsParameters)
            }
            requestOptions.queryParams = [:]
            parametersMap?.each { key,value ->
                requestOptions.queryParams.put(key.toString(),value?.toString())
            }


            def results = client.callApi(jenkinsUrl,path,jenkinsUser,jenkinsToken,requestOptions,'POST')
            if(results.success) {
                String queueId = results.content
                String queueUrl = results.headers['Location'] + 'api/json'
                requestOptions.body = null
                Integer attempts = 500
                log.info("Checking Queue: ${queueUrl}")
                while(attempts > 0) {
                    results = client.callJsonApi(queueUrl,null,jenkinsUser,jenkinsToken,requestOptions,'POST')
                    if(results.success && results.data?.executable?.url) {
                        break
                    } else if(results.success && results.data?.stuck) {
                        log.error("Build appears to be stuck")
                        return new TaskResult(
                                success: false,
                                data   : results.data,
                                output : results.content
                        )
                    }
                    sleep(5000l)
                    attempts--
                }
                if(results.success) {
                    log.info("Getting Queue Check: ${results.dump()}")
                    String buildUrl = results.data?.executable?.url + 'api/json'
                    attempts = 500
                    while(attempts > 0) {
                        results = client.callJsonApi(buildUrl,null,jenkinsUser,jenkinsToken,requestOptions,'POST')
                        if(results.data?.building == false && results.data?.result) {
                            if(results.data?.result != 'FAILURE') {
                                return new TaskResult(
                                        success: true,
                                        data   : results.data,
                                        output : results.data?.fullDisplayName
                                )
                            } else {
                                return new TaskResult(
                                        success: false,
                                        data   : results.data,
                                        output : results.data?.fullDisplayName
                                )
                            }
                        }
                        sleep(5000l) // 5 second pause
                        attempts--
                    }
                    return new TaskResult(
                            success: false,
                            data   : null,
                            output : "Timeout Occurred waiting for task status"
                    )

                } else {
                    return new TaskResult(
                            success: false,
                            data   : null,
                            error : "Error Checking Queue: ${results.error}"
                    )
                }
            } else {
                return new TaskResult(
                        success: false,
                        data   : null,
                        error : "Error Triggering Build: ${results.error}"
                )
            }
        } finally {
            client.shutdownClient()
        }
    }
}
