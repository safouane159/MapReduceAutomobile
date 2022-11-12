# MapReduceAutomobile
>HADOOP MAP REDUCE


# Sujet 
Après avoir construit votre DATA LAKE le Concessionnaire vous appelle et vous fait part que certaines don- nées étaient perdues avant votre intervention – notamment les détails sur l’émission CO2 / le coût d’énergie / la valeur de Bonus/Malus pour la taxation par marque et modelé de voiture. <br />
Il est possible que ses données seraient utiles pour améliorer la qualité de vos modelés prédictives. En cherchant sur Internet vous avez trouvé un fichier CO2.csv. C’est une autre base des données qui a certaines informations qui peuvent vous aider mais elle n’est pas parfaite. Elle ne contient pas tous les marques et modelés des voitures qui sont dans le catalogue du Concessionnaire. De plus le format de stockage est différent (la marque et le modelé sont dans une même colonne), il y a des valeurs manquant (colonne Bonus/Malus) et des valeurs erronés (colonne Bonus/Malus par exemple contient ‘-6 000€ 1’ a la place de ‘-6 000€’). <br />
Le but est d’écrire un programme map/reduce avec Hadoop ou Spark qui va permettre d’adapter le fichier- CO2.csv pour intégrer ses informations complémentaires dans la ou les tables catalogue du Concession- naire (ajouter des colonnes "Bonus / Malus", "Rejets CO2 g/km", "Cout Energie").
Notes :<br />
• Les modelés des voitures du fichier CO2.csv n’ont pas beaucoup des valeurs en commun par rapport à la table catalogue – on voudrait utiliser une valeur moyenne d’émission CO2 (de même pour les autres co- lonnes : "Bonus / Malus", "Cout Energie") pour la marque de voiture concerne.<br />
• Pour les marques de voitures qui ne sont pas dans le fichier CO2.csv on voudrait insérer la moyenne d’émission CO2 (de même pour les autres colonnes) de tous les marques de véhicules qui sont présent des deux côtés.<br />


**Cette tache est réalisé par  **<br />

| **Nom / mail**                               |  **Groupe**        |
|----------------------------------------------|----------------------------|
| SAMIA Oussama / oussamasamia1@gmail.com      | Gr8                        |
| OUAZRI Safouane / safouane1ouazri@gmail.com  | Gr8                        |
| KHALIFA HASSEN / $$$$$$$@gmail.com           | Gr8                        |
| MESSAI Raoua / $$$$$$$$$$$$$$$$$$@gmail.com  | Gr8                        |



## 1. Prise en main ( script pour executer les jobs)

### 1.1 se connecter au server a distance avec SSH :

ouvrir un nouveau terminal1 :<br />

```shell
$ ssh OUAZRI@134.59.152.114 -p 443
```
note : ( le mdps est "etuMia024NoSqlBs" ) 


### 1.2 ajouter le fichier CO2  et catalogue dans le server:

ouvrir un autre terminal2 :
    
```shell
$ scp -P 443 <path-to-your-file>/CO2.csv OUAZRI@134.59.152.114:~/

$ scp -P 443 <path-to-your-file>/catalogue.csv OUAZRI@134.59.152.114:~/
```
note : ( le mdps est "etuMia024NoSqlBs" ) 


### 1.3 ajouter le fichier CO2  et catalogue dans HDFS:


D'abord il faut crée un dossier dans hdfs 

```shell
$ hdfs dfs -mkdir /CO2_OUAZRI
```
verifier que le dossier exist 

```shell
$ hadoop fs -ls /CO2_OUAZRI
```

aprés on ajoute le fichier CO2 et catalogue dans hdfs  

```shell
$ hadoop fs -put CO2.csv /CO2_OUAZRI
$ hadoop fs -put catalogue.csv /CO2_OUAZRI
```

Verifier que les fichiers exist : 

```shell
$ hadoop fs -ls /CO2_OUAZRI
```
### 1.4 Deplacer les jar dans le server 

d'abord récuper les jars dans ce repository git 


```shell
$ git clone https://github.com/safouane159/MapReduceAutomobile.git
```

aprés deplacer les jar dans Hadoop 

```shell
$  scp -P 443 <path-to-jar>/Automobile-2.0.0.jar  OUAZRI@134.59.152.114:~/
$  scp -P 443 <path-to-jar>/AutomobileMultupleInput-1.0-SNAPSHOT.jar OUAZRI@134.59.152.114:~/
```

### 1.5 Executer les jars


```shell
 $ hadoop jar Automobile-2.0.0.jar  org.example.Automobile /CO2_OUAZRI/CO2.csv /result
 $ hadoop jar AutomobileMultupleInput-2.0.jar  org.mbds.AutomobileMultiple /CO2_OUAZRI/catalogue.csv  /result/part-r-00000 /myres
```

Important : il faut changer le nom de fichier des resultat "/results" et "/myres" si les deux deja exist 

### 1.6 Consulter les resultat

```shell
$ hadoop fs -cat /result/*
$ hadoop fs -cat /myres/*
```



## 2. Explication des script utilisé

dans cette section nous allons décrir notre démarche effectué pour l’adaptation du fichier CO2.csv et son intégration dans la table catalogue.


comme vous avez consaté nous avons executer deux job Map/Reduce pour arriver au resultat attendu.

  - Automobile-2.0. (Data Munging)
  - AutomobileMultupleInput-2.0. (joining two data set)

nous allons detailler chaqu'unz des deux dans ce chaptire


### 2.1. premiere job (Automobile-2.0)

cette job est responsable de faire le netoiyage des valeurs du fichier C02.csv, remplir les données manquane,calculer le moyen general 


Nettoyage

// ![Nettoyage](/images/A22.jpeg)

- les numero de ligne sont supprimer 
- les model sont supprimer car ils ne servent plus a rien puisque ils matche pas les models dans la table catalogue 
- les marque repsentra le KEY pour notre MAP 
- les BonusMalus, rejets CO2 et le cout repsentra le VALUE pour notre MAP
- a ce stage es valeur manquant du colonne BonusMalus seront replacer par 0 mais just temporairement (on expliquera plus tard dans le rapport les changement effectuer)



Example : Simulation job1 MapReduce
 ![job1](/images/A23.jpeg)

- dans chaque ligne ( iteration ) 3 ligne sont écrits : <br />
    -  ligne 1 pour qualquler la moyen des colonnes de chaque marque. <br />
    -  ligne 2 pour qualquler le moyen general de tout la table qui sera affecter pour les marques qui sont disponible dans catalaogue mais pas disponible dans le fichier CO2. <br />
    -  ligne 3 pour qualquler le moyen des BonusMalus qui sera affecter au marque qui on aucune valeur : cette ligne est special, elle a comme key AAAA pour assurer qu'elle sera traiter au premier par le reducer. <br />
    - ligne 3 est notre soltion pour passer une valeur ( moyen bonusMalus) depuis le mapper vers le reducer. <br />
    - les marque qui ont au moin une valeur BonusMalus n'auront pas la valeur moyen. <br />

- la valeur la plus grande dans l'ensemble de valeur "AAAA" correspends au moyen de BonusMalus. <br />
      
resultat : <br />
 ![job1](/images/z22.png)

      
### 2.2. deuxieme job (Automobile-2.0)

cette job est responsable de faire la jointure entre la table catalogue et la table CO2_Moyennes.

 ![job1](/images/b22.jpeg)


- Une jointure sera effectuer avec l'id marque.
- les marque qui n'existe pas dans  la table CO2_moyennnes auront le moyenne de toute la table (id = forall).<br />



Example : Simulation job1 MapReduce
 ![job1](/images/b23.jpeg)

- un mappeur séparé pour chacun des deux ensembles de données, c'est-à-dire un mappeur pour l'entrée Catalogue et l'autre pour l'entrée moyennes_CO2. <br />
- lire l'entrée en prenant un tuple à la fois. <br />
- Ensuite, tokeniser chaque mot de ce tuple et récupérer la marque voiture. <br />
- La marque sera ma clé de la paire clé-valeur que mon mappeur générera éventuellement. <br />
- J'ajouterai également un tag "catalogue" ou "CO2" pour indiquer que ce tuple d'entrée est de type catalogue ou CO2. <br />

note : pour la moyenne general de la table CO2 la clé est "AAAA" pour quelle sera traité en premier, on servira pour les marques qui n'existe pas dans la table CO2. <br />


Resultat : <br />

 ![job1](/images/z23.jpeg)

