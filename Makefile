MAIN = globair.Example
ZIP_NAME = cpl-dsl_Croes_DeCroock_Winant.zip

compile:
	mvn compile

run:
	mvn scala:run -DmainClass=$(MAIN)

jar:
	mvn compile assembly:single

clean:
	mvn clean

test:
	mvn test-compile scalatest:test

zip:
	cp report/report.pdf report.pdf
	zip -r $(ZIP_NAME) src resources test pom.xml Makefile report.pdf
	rm report.pdf

.PHONY: run jar clean test zip
