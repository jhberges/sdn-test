# sdn-test
JUnit test to show a regression issue for 3.3.0.M1.

The test jhberges.sdn.RelationshipEntityTest can be set up to use either embedded or remoted database, and should (I think?) yield same behavior?

The POJOs and algorithms are simplified but provide the gist of it.

The application in question ("container") is a Dropwziard application, and the template and stuff is *NOT* wired up by Spring - but rather in the fashion showed in the test.

