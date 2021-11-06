FROM openjdk:11.0.6-jre

ARG APP_HOME=/notes-app

WORKDIR $APP_HOME

COPY target/notes-jar-with-dependencies.jar $APP_HOME/notes.jar
# ADD target/lib $APP_HOME/lib

EXPOSE 27017
EXPOSE 8765

ENTRYPOINT java $JAVA_OPTS -Ddatabase.host=host.docker.internal -Dspring.data.mongodb.host=host.docker.internal -Dserver.host=host.docker.internal -Dlogging.level.root=INFO -jar ./notes.jar $JAVA_ARGS
