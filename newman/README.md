.. _newman Library: 
---------
Newman
---------

Newman is a command-line collection runner for Postman. It allows you to effortlessly run and test a Postman collection directly from the command-line. It is built with extensibility in mind so that you can easily integrate it with your continuous integration servers and build systems.

Steps Contributed
=================
* run_api_tests()


Library Configuration Options
=============================


.. csv-table::  SonarQube Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "run_api_test", "Tenant needs to specify whether they would like to run API testing or not", "false"
   


Example Configuration Snippet
=============================

.. code:: groovy

    toggle_tests{
            override = true
            skip_unit_tests = false
            run_sonarqube_tests = false
            run_api_tests = false

Sonar Scanner Configurations
============================

Extra configuration options can be leveraged please see URL newman-project.properties_

.. _newman-project.properties: https://docs.sonarqube.org/display/SONAR/Analysis+Parameters

External Dependencies
=====================

* There are no dependencies, tenant just needs to specify that they want to run API Testing. (run_api_tests = true)
=====================
