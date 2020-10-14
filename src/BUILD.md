# Publish to Nexus on Mac OSX

## prepare the password
update ~/.m2/settings.xml
```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
</settings>
```


## run the deployment with gpg profile
```
GPG_TTY=$(tty)
export GPG_TTY
mvn clean deploy -P gpg
```