MAIN = globair.DB

compile:
	mvn compile

run:
	mvn scala:run -DmainClass=$(MAIN)

jar:
	mvn compile assembly:single

clean:
	mvn clean

.PHONY: run jar clean
