Approval
---------

This library was created to provide a step for the pipeline to ask for permission to Deploy in specific environments. User will have the options to continue (Proceeed), or end the pipeline (Abort)

Steps Provided
##############

* ask_approval()


Library Configurations
######################


.. csv-table:: Ansible Library Configuration Options
   :header: "Field", "Description", "Default Value", "Optional"

   "message", "The message that will be defined in pipeline_config.groovy", "none", "yes"
   "approval_group", "Group defined as approver", "none", "no"


*****************************

.. code::

    staging{
        approval {
            message = "Deploy to Staging?"
            approval_group = "GSA_Jenkins_R_Approvers"


External Dependencies
#####################

* If the approval step is called, a message must be set in the pipeline_config.groovy file for tenant
