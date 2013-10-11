#FoodForThoughtSite

This is the website source for the Food for Thought (FFT) experiment, run by SO(3), the computer vision research group at UCSD. 
In the spirit of initiatives at leading tech companies, FFT provides free, high-quality, food for the members of the SO(3) research team. 
We expect this will increase member happiness and health. 
It should also increase research productivity by reducing the amount of time spent obtaining quality food and facilitating group interactions over meals.

The website will log the ongoing costs and benefits of the experiment as well as procedures and recommendations for its replication.

##Current address

The current address for the site is http://23.21.130.222.
Clearly, this isn't a permanent address, and will likely change in the
near future.

##Hacking

This project currently needs a custom version of
[SecureSocial](https://github.com/jaliss/securesocial).
See `.travis.yml` to see how it should be installed.

For the site to actually run, you will need to:

  * Start up a database and make the jdbc url in `conf/secrets.conf`
    point to it.
    See the database config section.
  * Get the GMail password from Eric to allow the site to send emails.

###Database config

If you want to use MariaDB and you're on Ubuntu, you can follow these
directions to get up and running:

  * [Install
    MariaDB](http://askubuntu.com/questions/64772/how-to-install-mariadb).
  * Log into the running MariaDB server with the command `mysql
    --user=root --password=<your password>`.
  * Create the FFTSite database by running the command `create database
    FFTSite;`.
  * Log out of the MariaDB console.
  * Edit conf/secrets.conf so the database configuration line looks
    like: `db.default.url="jdbc:mariadb://localhost:3306/FFTSite?user=root&password=<your password>"`

[![Build Status](https://travis-ci.org/emchristiansen/FoodForThoughtSite.png)](https://travis-ci.org/emchristiansen/FoodForThoughtSite)

##TODO

Customize the email messages.


