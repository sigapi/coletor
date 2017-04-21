FROM gcr.io/google_appengine/openjdk

RUN apt-get update \
    && apt-get install --no-install-recommends -y \
        openjfx

VOLUME /tmp
COPY sigapi-coletor.jar app.jar
CMD [ "java","-Dfile.encoding=UTF-8","-jar","/app.jar"]
