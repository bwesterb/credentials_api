* Credentials API

The Credentials API is the high-level interface to the credential system. The
API abstracts away from the low-level details involved in the communication
between the card, the terminal and the library. It offers a uniform interface
with the following two basic methods:

*Issue* The issuer provides the API with a specification of the credential to 
   be issued and the attribute values it should contain. The API then takes
   care of the protocol specific communication using the terminal application
   and the cryptographic library. The result of this action is either succes or
   failure.

*Verify* The relying party provides the API with a specification of the
   attributes it wants to verify and the credential they belong to. The API
   then uses the terminal application to request the attributes and a proof of
   validity from the card. Finally, the API will use the cryptographic library
   to verify the proof and it will either report failure, or it will return the
   revealed attributes to the relying party.

