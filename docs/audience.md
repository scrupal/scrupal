## Audience
This article explains the target audience for Scrupal - who should use it and why. We'll first examine the use cases
Scrupal was intended for and how that affected its design. Then we'll review some examples of the places where Scrupal
might be useful.

### Relevant Features

#### Privacy
Scrupal doesn't utilize cloud enabled service for anything because it is assumed that Scrupal users demand a high level
of privacy for their data. Any service that would require Scrupal to send its users data over the Internet was
precluded for the simple reason that if Scrupal never re-transmits its users data, that data cannot be intercepted.
The only place the data is retransmitted is to another user, over secured web connections.

#### Validity
Scrupal has an extensive data validation framework. Because Scrupal does not have a fixed data model, it must validate
data structures that were not conceived when Scrupal was written. Such flexibility often comes at the price of data
corruption: a lack of data validation (upon input) can allow malfeasant or ignorant users to enter information that
could cause a system to fail. To eliminate this possibility, Scrupal thoroughly validates all data,
provides mechanisms for migrating data structures as they change, and requires its users to always specify the
structure of the intended data.

#### Interactivity
Scrupal is designed to be highly interactive. Its design permits users to chat with one another, participate in joint
development, design or construction activities, mashup data from multiple sources, and create unique data
representations that can be shared with others instantly.

#### Verity
Scrupal's Verity module implements a system of information assessment by its users. Users can vouch for the verity of
others information, or vilify it. Users can tell Scrupal what they think about each piece of information through the
use of several dichotomies. Users select a value from 1 to -1 to represent where on the spectrum between two choices
their opinion lies. For example:
* Love/Hate
-- loves (1), likes (0.5), is dispassionate about (0), disklikes (-0.5), hates (-1)
* True/False
-- True (1), Mostly True (0.5), Unsure (0), Mostly False (-0.5), False (-1)
Scrupal supports a standard set of such measures, and allows adminstrators to define additional measures that are
applicable to specific information domains. As users put forth information and others assess that information,
it becomes clear quickly who is generating useful information and who is not.

#### Discourse
Scrupal's Discourse module provides a real-time, highly interactive discussion forum that utilizes Verity and Validity
features to ensure that spammers, scammers, idiots and other malfeasants simply don't have a seat at the table.

### Community Leaders

### SaaS Companies

### Clubs, Groups, Societies, Organizations

### Example 1: Country Club

### Example 2: Intranet Knowledgebase
The intranet for a service organization needs a knowledgebase that is maintained actively by the various field agents
roaming the world. Because these information workers depend on accurate, verifiable information that is in constant
flux, a system of information validation was needed. The Verity feature of Scrupal was employed to

- - -
<sub><sup>&copy; Copyright 2013, Reid Spencer. All Rights Reserved.</sup></sub>
