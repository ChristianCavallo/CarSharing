# Car Sharing
Progetto per il corso di Distributed Systems and Big Data della laurea magistrale LM-32, Università degli Studi di Catania.

Sviluppatori: **Noemi Buggea**, **Christian Cavallo**.

##

Si tratta di un servizio di car sharing (es. Enjoy) sviluppato come sistema distribuito con Spring Boot. Per il testing locale abbiamo utilizzato Docker, 
mentre per il testing su cluster abbiamo usato Kubernetes su MiniKube.


Comprende i seguenti micro servizi:
-	**Users Manager**: si occupa della registrazione e login degli utenti. È anche implementato un sistema di autenticazione che utilizza i JWT (JSON Web Tokens).
-	**Car Manager**: si occupa della simulazione “blocco/sblocco” dei veicoli su richiesta di un utente.
-	**Payment Manager**: si occupa di gestire il pagamento di una fattura relativa ad un noleggio. Utilizza un IPN Simulator (https://github.com/ChristianCavallo/ipn_simulator) appositamente sviluppato per simulare la presenza di un server PayPal.
-	**Rental Manager**: si occupa della gestione di un noleggio (creazione, terminazione).
-	**Invoice Manager**: si occupa di generare una fattura al momento della terminazione di un noleggio.
-	**Logging System**: si occupa di ricevere i messaggi sul topic “log” e memorizzarli in un formato adatto all’interno di un DB.
-	**Gateway**: si occupa di direzionare le richieste verso il corretto micro servizio (mediante discovery).


Per maggiori dettagli sulla struttura e funzionamento del progetto, è possibile consultare la relazione: https://github.com/ChristianCavallo/CarSharing/blob/master/Relazione%20CarSharing.pdf
