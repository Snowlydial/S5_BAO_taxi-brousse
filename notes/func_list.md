# Considering adding:
- With BusVoyage, the same bus can redo the same voyage on the same day but different hour
    - Condition: when creating BusVoyage, take into account of the 1st heureDepart, the bus becomes available for service again after heureDepart+Voyage.getDuree*2 (multiply by 2 cuz it has to come back to where it began)...mayde

# Week of [09/01/26]
1. Regle de gestion
- We have many cars
- Each car have their own capacity (== Max Nb place)
- One Voyage is defined by its gareDepart and gareArrivee
- A Voyage can be executed by many car
- A Voyage can be executed many times in a day
- Un trajet can be done on multiple days
2. Fonctionnalites
-> Scenario: One client want to do reservations for a Voyage between Gare Fasankarana - Gare Ambolomadinika for January 14th at 2PM (14h)