# scmsynchronizer

## Goal
Provide scm changeset lists for Jira 7 just like we had in jira 5 where each changeset lists branch information allowing users to easily determine when commits merged between branches.

## Overview
[Github organization and repository webhooks](https://developer.github.com/v3/repos/hooks/) produce a json file containing branch and commit data and [SCM Activity Plugin REST API](https://github.com/scmenthusiast/jira-scm-activity-plugin/wiki/JIRA-SCM-Activity-Plugin-REST-API) accepts a different format.  Therefore, we 
* create an http server as the webhook endpoint
* translate the json into SCM Activity format
* use a REST post call to push the commits to Jira
* SCM Activity plugin stores commit data in a separate mysql DB and presents it on an SCM Activity tab


## [Server](https://github.com/attivio/scmsynchronizer/blob/master/src/main/java/com/attivio/releng/scmsync/Server.java)
We use [Spark Java Framework](http://sparkjava.com/documentation.html) as an easy way to create a webserver that can accept post requests from the webhook.  [Gson](https://github.com/google/gson) converts the webhook json into java objects which we transform into SCMActivity format and deliver to the REST api via [Jersey library](https://jersey.java.net/).  

Spark Java Framework in [Server code](https://github.com/attivio/scmsynchronizer/blob/master/src/main/java/com/attivio/releng/scmsync/Server.java) uses java 8's lamba feature to easily create handler for post:


     post("/payload",(req, res) -> {
      if (! hostAllowed(req.ip())) {
        String message= "Unauthorized Host Denied: " + req.ip();
        logger.error(message);
        return message;
      }
      String result = processCommits(req.body());
      return result;
     });

Server reads from its [scmsync.properties](https://github.com/attivio/scmsynchronizer/blob/master/src/main/resources/scmsync.properties) file to obtain the following settings:
* **port** - port on which to listen
* **allowed.hosts** - which hosts may access Server's post handler
* **jira.url** - base url for jira (e.g., https://jira.company.com)
* **jira.user** / **jira.password** - auth information for connection

[Application Assembler Maven Plugin](http://www.mojohaus.org/appassembler/appassembler-maven-plugin/) copies dependencies into kit/lib dir and creates startup scripts for windows & linux.  We pass in a '-Dscmsync.home.dir=@BASEDIR@' to allow the application to locate its configuration files and bundle an init.d startup script.

## Handling Github Enterprise Json data

Gson library parses json and populates plain old java objects.  In [GithubEnterprise package](https://github.com/attivio/scmsynchronizer/tree/master/src/main/java/com/attivio/releng/scmsync/GithubEnterprise) we break down the payload thus:
* [Payload.java](https://github.com/attivio/scmsynchronizer/blob/master/src/main/java/com/attivio/releng/scmsync/GithubEnterprise/Payload.java) transforms json via createFromFile or createFromString
* [Author.java](https://github.com/attivio/scmsynchronizer/blob/master/src/main/java/com/attivio/releng/scmsync/GithubEnterprise/Author.java), [Commit.java](https://github.com/attivio/scmsynchronizer/blob/master/src/main/java/com/attivio/releng/scmsync/GithubEnterprise/Commit.java), [Repository.java](https://github.com/attivio/scmsynchronizer/blob/master/src/main/java/com/attivio/releng/scmsync/GithubEnterprise/Repository.java) handle the inner json data from payload

Github and SCM Activity use difference field names date formats.  Server's **processCommitts* method uses Payload to parse the json and Payload's **getScmActivities* to generate one activity object per jira ticket per commit.  If a payload contains two commits where the first's message is "JIRA-1 implented JIRA-2" and the second's contains "JIRA-4 typo fixed" then we expect three activity objects, one for each mentioned ticket.  The majority of getters and setters in the classes support easy testing and aren't used in production.

Server iterates through the list of activities sending each via **jirasend** method.  This creates a Jersey client, connects to the REST API access point (**/rest/scmactivity/1.0/changeset/activity**) and post's the activity.   Server captures errors and reports them both to its log and back to github webhook via the response.

## Files
* <install dir>/scmsync.log - log file
* <install dir>/log4j.xml - log config
* <instal dir>/scmsync.properties - connecting and permissions settings
* <install dir>/bin/  - scripts for startup and init.d