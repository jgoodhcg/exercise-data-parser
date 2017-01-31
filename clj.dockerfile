FROM ubuntu:latest

WORKDIR /exercise-data-parser
COPY ./ /exercise-data-parser

RUN apt-get update
RUN apt-get install curl -y
RUN apt-get install -y openjdk-8-jre-headless
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o /bin/lein
RUN chmod a+x /bin/lein
RUN lein

WORKDIR /exercise-data-parser
CMD lein update-in :dependencies conj "[org.clojure/tools.nrepl \"0.2.12\"]" -- update-in :plugins conj "[refactor-nrepl \"2.3.0-SNAPSHOT\"]" -- update-in :plugins conj "[cider/cider-nrepl \"0.13.0-SNAPSHOT\"]" -- repl :headless :host 0.0.0.0 :port 7888
