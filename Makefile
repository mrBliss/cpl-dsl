MAIN = globair.Main

compile:
	mvn compile

run:
	mvn scala:run -DmainClass=$(MAIN)

jar:
	mvn compile assembly:single

clean:
	mvn clean

test:
	mvn scalatest:test

.PHONY: run jar clean test
