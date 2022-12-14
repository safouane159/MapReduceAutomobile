# MapReduceAutomobile
>HADOOP MAP REDUCE


# Sujet 
Après avoir construit votre DATA LAKE le Concessionnaire vous appelle et vous fait part que certaines don- nées étaient perdues avant votre intervention 
– notamment les détails sur l’émission CO2 / le coût d’énergie / la valeur de Bonus/Malus pour la taxation par marque et modelé de voiture. <br />
Il est possible que ses données seraient utiles pour améliorer la qualité de vos modelés prédictives. En cherchant sur Internet vous avez trouvé un fichier CO2.csv. 
C’est une autre base des données qui a certaines informations qui peuvent vous aider mais elle n’est pas parfaite. Elle ne contient pas tous les marques et modelés des voitures qui sont dans le catalogue du Concessionnaire. De plus le format de stockage est différent (la marque et le modelé sont dans une même colonne), il y a des valeurs manquant (colonne Bonus/Malus) et des valeurs erronés (colonne Bonus/Malus par exemple contient ‘-6 000€ 1’ a la place de ‘-6 000€’). <br />
Le but est d’écrire un programme map/reduce avec Hadoop ou Spark qui va permettre d’adapter le fichier- CO2.csv pour intégrer ses informations complémentaires dans la ou les tables catalogue du Concession- naire (ajouter des colonnes "Bonus / Malus", "Rejets CO2 g/km", "Cout Energie").
Notes :<br />
• Les modelés des voitures du fichier CO2.csv n’ont pas beaucoup des valeurs en commun par rapport à la table catalogue
 – on voudrait utiliser une valeur moyenne d’émission CO2 (de même pour les autres co- lonnes : "Bonus / Malus", "Cout Energie") pour la marque de voiture concerne.<br />
• Pour les marques de voitures qui ne sont pas dans le fichier CO2.csv on voudrait insérer la moyenne d’émission CO2 (de même pour les autres colonnes) de tous les marques de véhicules qui sont présent des deux côtés.<br />


**Cette tache est réalisé par  **<br />

| **Nom / mail**                               |  **Groupe**        |
|----------------------------------------------|----------------------------|
| SAMIA Oussama / oussamasamia1@gmail.com      | Gr7                        |
| OUAZRI Safouane / safouane1ouazri@gmail.com  | Gr7                        |
| KHALIFA HASSEN / $$$$$$$@gmail.com           | Gr7                        |
| MESSAI Raoua / raouamessai98@gmail.com       | Gr7.                       |



## 1. Prise en main (script pour executer les jobs)

### 1.1. Se connecter à la machine virtuelle avec SSH :

Ouvrir un nouveau terminal1 :<br />


- Start VM : 

```shell
$ cd vagrant-projects/OracleDatabase/21.3.0
$ sudo vagrant up
```

- Connect VM via SSH :<br />

```shell
$ vagrant plugin install vagrant-scp (only if the plugin is not installed) 
$ sudo vagrant ssh
```


- start Hadoop :<br />

```shell
$ start-dfs.sh
```
### 1.2. Ajouter les fichiers CO2  et catalogue dans le server:<br />

Ouvrir un deuxième terminal2 :<br />
    
```shell
$ sudo vagrant scp <path-to-csv>/CO2.csv oracle-21c-vagrant:/home/vagrant/gr8
$ sudo vagrant scp <path-to-csv>/catalogue.csv oracle-21c-vagrant:/home/vagrant/gr8
```



### 1.3. Ajouter le fichier CO2  et catalogue dans HDFS: <br />


D'abord il faut créer un dossier dans hdfs <br />

```shell
$ hdfs dfs -mkdir /CO2_OUAZRI
```
Vérifier que le dossier existe <br />

```shell
$ hadoop fs -ls /CO2_OUAZRI
```

Après  on ajoute les fichiers CO2 et catalogue dans hdfs  

```shell
$ hadoop fs -put CO2.csv /CO2_OUAZRI
$ hadoop fs -put catalogue.csv /CO2_OUAZRI
```

Vérifier que les fichiers existent : <br />

```shell
$ hadoop fs -ls /CO2_OUAZRI
```
### 1.4. Déplacer les jar dans le server <br />

D'abord récupérer les jars dans ce repository git <br />


```shell
$ git clone https://github.com/safouane159/MapReduceAutomobile.git
```

Après déplacer les jar dans Hadoop <br />

```shell
$ sudo vagrant scp <path-to-jar>/Automobile-2.0.0.jar oracle-21c-vagrant:/home/vagrant/gr8
$ sudo vagrant scp.<path-to-jar>/AutomobileMultupleInput-2.0.0.jar oracle-21c-vagrant:/home/vagrant/gr8

```

### 1.5. Exécuter  les jars <br />


```shell
 $ cd /home/vagrant/gr8
 $ hadoop jar Automobile-2.0.0.jar  org.example.Automobile CO2_OUAZRI/CO2.csv /result

 $ hadoop jar AutomobileMultupleInput-2.0.0.jar  org.mbds.AutomobileMultiple CO2_OUAZRI/catalogue.csv  /result/part-r-00000 /myres
```

Important : il faut changer le nom du fichier de Résultat  "/result" et "/myres" si les deux existent déjà 

### 1.6. Consulter les Résultats

```shell
 $ hadoop fs -cat /result/*
 $ hadoop fs -cat /myres/*
```



## 2. Explication des scripts utilisés

Dans cette section, nous allons décrire les étapes que nous avons suivies pour adapter le fichier CO2.csv et l'intégrer dans la table du catalogue.

Comme vous avez consaté nous avons executé deux job Map/Reduce pour arriver au Résultats attendu.

  - Automobile-2.0. (Data Munging).
  - AutomobileMultupleInput-2.0. (joining two data set).

Nous allons détailler chaqu'une des deux dans ce chaptire


### 2.1. Premiere job (Automobile-2.0)

Ce Job est chargée de nettoyer les valeurs du fichier C02.csv, de compléter les données manquantes et de calculer la moyenne générale. <br /> 


Nettoyage: <br />

 ![Nettoyage](/images/A22.jpeg)

- Les numéros de ligne sont supprimés.
- Les modèles sont supprimés car ils ne sont plus utiles, puisqu'ils ne correspondent pas aux modèles de la table du catalogue.
- La marque sera la KEY pour notre MAP.
- Le BonusMalus, les émissions de CO2 et le coût représenteront la VALEUR de notre MAP.
- À ce stade, les valeurs manquantes de la colonne BonusMalus seront remplacées par 0, mais seulement de manière temporaire (nous expliquerons plus tard dans le rapport les modifications apportées).<br />



Example : Simulation job1 MapReduce <br />

 ![job1](/images/A23.jpeg)

- Dans chaque ligne ( iteration ) 3 lignes sont écrites : <br />
    -  Ligne 1 pour calculer la moyenne des colonnes de chaque marque. <br />
    -  Ligne 2 pour calculer la moyenne générale de toute la table qui sera affectée pour les marques qui sont disponibles dans catalogue mais pas disponibles dans le fichier CO2. <br />
    -  Ligne 3 pour calculer la moyennes des BonusMalus qui sera affectée au marques qui ne possèdent aucune valeur : cette ligne est special, elle a comme key "AAAA" pour assurer qu'elle sera traiter au premier par le reducer. <br />
    - Ligne 3 est notre solution pour passer une valeur ( moyennes bonusMalus) depuis le mapper vers le reducer. <br />
    - Les marques qui ont au moins une valeur BonusMalus n'auront pas la valeur moyennes. <br />

- La valeur la plus grande dans l'ensemble des valeur "AAAA" correspend à la moyenne de BonusMalus. <br />
      
Résultat : <br />

 ![job1](/images/z22.png)

      
### 2.2. Deuxieme job (Automobile-2.0)

Cet job est responsable de faire la jointure entre la table catalogue et la table CO2_Moyennes.<br />

 ![job1](/images/b22.jpeg)


- Une jointure sera faite avec l'id marque.
- Les marques qui n'existent pas dans la table CO2_moyennnes auront la moyenne de toute la table (id = forall).<br />



Example : Simulation job1 MapReduce <br />

 ![job1](/images/b23.jpeg)

- Un mappeur séparé pour chacun des deux ensembles de données, c'est-à-dire un mappeur pour l'entrée Catalogue et l'autre pour l'entrée moyennes_CO2. <br />
- Lire l'entrée en prenant un tuple à la fois. <br />
- Ensuite, tokeniser chaque mot de ce tuple et récupérer la marque voiture. <br />
- La marque sera la clé de la paire clé-valeur que mon mappeur générera éventuellement. <br />
- J'ajouterai également un tag "catalogue" ou "CO2" pour indiquer que ce tuple d'entrée est de type catalogue ou CO2. <br />

Note : pour la moyenne générale de la table CO2 la clé est "AAAA" pour qu'elle sera traitée en premier, on servira pour les marques qui n'existe pas dans la table CO2. <br />


Résultat  : <br />

 ![job1](/images/z23.jpeg)


3. Scripts (Programmes)

- Vous trouverz dans ce repositorie:
    - Les scripts utilisé. Le code est commenté est bien detaillé.
    - Les fichier de Résultat  de chaque job
    - Les jars à exécuter


