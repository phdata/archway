# Cloudera

## Parcels

The easiest way to install and use Archway is with a Cloudera parcel and Cloudera Service Descriptor (CSD).

A CSD will allow you to add Archway as a service into a Cloudera Manager instance. A parcel is used to distribute
binaries to the node Archway will run on.

Find the latest CSD and parcel versions for Archway pre-built here: https://repository.phdata.io/artifactory/list/parcels-release/phdata/archway/

### Installing the Archway CSD

1. Download to CSD and place in the csd directory, usually `/opt/cloudera/csd`
2. Make sure it is readable by Cloudera Manager `chown cloudera-scm:cloudera-scm /opt/cloudera/csd/ARCHWAY*`
3. Restart Cloudera Manager to enable the new csd `sudo service cloudera-scm restart`

### Installing the Archway Parcel

1. In Cloudera Manager, click on the parcels
2. Click on 'Configuration' and add a parcel repo, for example: https://repository.phdata.io/artifactory/list/parcels-release/phdata/archway/2.2.2/
3. Choose your cluster. The newly added parcel should appear soon
4. Download, Distribute, and Activate the parcel

The service can now be installed from your cluster using the 'Actions -> Add Service' wizard
