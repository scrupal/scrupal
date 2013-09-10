## Scruples
The word scruple means *a moral or ethical consideration or standard that acts as a restraining force or inhibits
certain actions*. In the context of the Scrupal software, our "moral or ethical consideration or standard" has to do
with fundamental technical and sociological principles of computing systems and the people that use them. Those
principles have restrained and inhibited the Scrupal developers from the outset to ensure that
Scrupal has scruples. Here's what they are.

### Testability
When we write code, we write the test plan first. That is, we use Test Driven Design. By using specs2 this serves as
a way to communicate about future features as well since it is so easily read and written. We use the following
scruples:
* Every code module has a competent test suite written for it that covers at least 80% of the code.
* The master branch always passes all tests, no exceptions.
* Code changes that cause test suites to fail will be reverted with impunity.
* Test code is written before the code we develop.
* Attempts to subvert the above will cause a developer to lose his commit privileges.


### Scalability

### Security

### Flexibility
A data model that is highly extensible so it can adapt to unforeseen uses.

### Simplicity - You can set up a web site without ever writing a line of code.

- - -
<sub><sup>&copy; Copyright 2013, Reid Spencer. All Rights Reserved.</sup></sub>
