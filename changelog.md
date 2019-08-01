## 1.7.1

- Fix bug where yarn node manager and resource manager links would fail causing the all cluster info to fail. c74955361a62c9655324257593de6571434655d1

## 1.7.0

### Enhancements

- Users can now see the workspaces they are a part of, even if they are not the manager of that workspace. 0d7ba505af07a6527979bbff3482d179c47d681e
- The 'Cluster Status' icon now links directly to CM so you can immediately satisfy your curiosity when there is a
  bad CM health status. ef2f0f3e3a40f075688419451f39ec7bab536ca9
- Custom Templates can now be added to the template root dir under the 'custom' folder. Custom templates will
  show as another option on the workspace request 'Behavior' page. See 'Breaking Changes' below for instructions on creating a custom template and modifying existing templates. 9f0d4415fbbe5cf0903fec540f238d2b79c15933
- Package the Impala JDBC driver with the application. It is no longer to download the Impala driver and add it to
  the classpath manually. 8a8d63c539c04bcb50d85c8eaaa9ab8783ffd298
- Add the database migration 'flyway' command as a role command. Before installing the service for the first time,
  and when there are database migrations, run the 'Migrate Database' command. e4f6c93056c15822e3e13e847826fe55e175149c
- Add 'heimdali.additional.java.options' safety valve style parameter to the service for adding additional Java options
  to the service. fe6262993572bd30a3c06a8f84b0069f9329c981
- Show a workspace status of either 'provisioning' or 'pending' that is linked to the actual provisioning status of
  the workspace. The old 'READY' icon in the upper left of the workspaces has been removed, it was only tied to workspace approvals and could be misleading about the status of a workspace. c805625825c5500e6069e9011de6dcbe4025dc37
- Verify database connection during Heimdali startup for faster fail and easier debugging of configuration. 7c0848dfdabb3be65f0b6a55dc356e851088d18e
- Add a 'MANAGE' tab to the workspace details page. the Manage tab has 'provision' and 'deprovision' buttons.
  'Deprovision' is an experimental feature, it may not deprovision all resources in your workspace. 039d265ef6145f2e915a260d1ee8909f8fab9f25
- Add validation for SMTP authentication settings: If you choose authenticated SMTP but don't set the username
  and application the application will fail on startup. 3aba1a503065a02113225959edc95a5cb3b4a4a6
- Add Oracle database support. Oracle database support is handled automatically by the application 'Database Migration'
  command, no steps are necessary except setting the correct connection string, driver, username, and password for your Oracle database. 9e8d76ed6725814e68e475720721f6be3d0f27e3
- Dynamically generate the application copyright year. 93b08baf5e612601f82ab99a5219491b34b640e5
- Pull yarn ports from Cloudera Manager instead of the hadoop config files. ffd21b3be690de4cd73a4fd831de755ec9c899eb
- Make the 'lookup' ldap connection optional. If the lookup connection server is left empty, the provisioning connection
  will be used. e407508fb7c1de23f59b2abf99380df4a9a45d3b

### Bug Fixes

- When adding a member to a workspace, users are checked for exsitence in Active Directory before the user is added to
  the workspace. There is a spinner that shows until this check is completed and the user is added to the workspace
- Fix noisy http4s client error messages. abee7743f1e10ac23a52dcce8a1299dccb1d74b2
- Remove CORS headers. 85d0a585bcc903a790ac5dfd6ad3b862d3f58008
- Show better messages when provisioning fails to invalidate Impala metadata. fdeb55451cd031c04cb7e68d67fb6d8da7be9e37
- Handle backslashes in user DNs. bea55dae6698bee30a4ac34baad05ed94b9e2210
- Additional error handling and logging of error messages everywhere we could. b5041f8ba633db702e7f08b49cd49911436d9659
- Use a minimum Kafka topic replication factor of '3', regardless of what is defined in the template. 6e19a5343f832954e5f8559c9e655659b8cbc0af
- Fix Cloudera Manager port validation error when the two ldap connections used the same port. 0bc79b3d2c5fdfba1d8ee20c00749acf720c4f37
- When deprovisioning a workspace, don't fail the deprovision when the hdfs dir does not exist. 215f4a23413122eb207f0f4e4802de4f194edb5c

## CSD Upgrade Note

A CSD upgrade should not be necessary, but since there are so many documentation and usability improvements updating the CSD is recommended

### Breaking Changes

**Template Changes**
You must update existing templates to contain a 'metadata' block. If you do not update this block, your current templates 'user.ssp', 'structured.ssp', and 'simple.ssp' will fail to work. The metadata block descriptions are not important and the values do not need to be customized at this time.

A metadata block looks like this:

```
  "metadata": {
    "name": "${template.name}",
    "description": "${template.description}",
    "ordering": 1,
    "tags": {}
  },
```

A full template example can be found here: https://bitbucket.org/phdata/heimdali/src/8f588dadbc60b04bbfcc1c5a71b40c6626fb3464/templates/src/main/ssp/default/simple.ssp#lines-6

## 1.6.4

- Fix error when requesting workspaces with special characters in workspace requests. 220800ea3dbe592fee9f49b8611b2bc8994f2314

## 1.6.3

- Add extra error handling to controllers. 2487c8dd2c83504e91174df7492b4a609ea70878
- Lowercase usernames when creating home directories. 2ef1e422b8c35583a6317814ece5e9c6cfee8100

## 1.6.2

### Bug Fixes

- Fix json parsing in template requests where the user DN has a backslash 47fab50f8f9514745a8ce50921c66781aadb4f14, HEIM-364

## 1.6.1

### Bug fixes

- Handle the case where no Navigator installation is available

##1.6.0

### Bug Fixes

- Fix a bug where personal workspaces would not have their databases created. commit 2936b19792ae721b5d2b7f55c205e71e63ce44a6, HEIM-275 (pull request #87)
- Put 'application' and 'messaging' tabs behind feature flags. They can be enabled when they are known to be stable. commit fb2fc9892e4fedf649fe2ae67c4a6762ac674829, HEIM-284 (pull request #85)
- Fix a bug where a users displayname was used to create their database and user directory. 74d3371b0ac62c3b894428aa9d82b516a68b654b, HEIM-277 (pull request #86)
- Fix a bug where the `dfs.nameservice` was assumed to be the name of the cluster in CM
- Remove hardcoded parcels root for customers with a customized parcels root. cde5df53e11875e0bf58f186092c653d34599830, HEIM-103 (pull request #64)
- Fix 'Connected to Unknown Cluster', instead show a spinner then show nothing.
- Application server will now exit if there is a problem parsing a bad config. e1c9f4bbcb4e7172ea99bb632bd462f3921dd97f, HEIM-262 (pull request #62)
- Made tag lines on login and home page match. afc0165464a92ef0710321331c42248fe0bbe0c2, feature/HEIM-246 (pull request #40)
- Increased verbosity of startup logs so you can see if something is hung or the server has started. 8e6c077011571c4bbae2120ae4fe492a0625d0ae
- Silence some overly verbose 'scalate' classpath warnings in the logs. d183da7ba8b97818ee8248217ca32d0fa7904719
- Add more logging when on an ldap failed login. 56a58fdddac313b538bb79f33afc0fe81d0d3e32
- Better error message when there is a server error vs invalid credentials entered on the login page. d13a62712ca8f65f979c4af32a319a316b8154ec, HEIM-199 (pull request #8)
- Fix some log statements going to stderr instead of the logger. 0648e81102fde8631cb0bc5ad31024b460b71fc2
- Fix a bug where connections to active directory were never closed. 65a1006eba52e2614f7b2c3e137ceb9b292eb0ce
- Fixed a bug where emails failed to send. 501950504332192436457e036751d635d341e5a9
- Fixed a bug where the final 'Request' button on a workspace would hang. 501950504332192436457e036751d635d341e5a9
- Fixed a bug where invalid users could be added to a workspace. 77cff801a59bbc9faaa614e88911cdb7523c7913

### Product Enhancements

- Create a test table 'heimdali_test' in each database created. This test database is used for Impala metadata invalidations and to demonstrate the database has been created. 4a8455b256181092d3b52d5e19b1cce7b192b5d8 HEIM-290 (pull request #82)
- Serve UI code from the API. Previously there was a Python process serving the UI code on a different port. This caused issues with CORS and the spnego implementation. This also means a user doesn't need to accept self signed certificates for both ports when using self signed certificates
- Removed the UI CSD role
- Invalidate Impala metadata after a new workspace is created. e3da7a707a13a790df22946ab75e200a255f30e1, HEIM-112 (pull request #31)
- Invalidate Impala metadata when a new user is added to a workspace
- Implemented SPNEGO authentication. Spnego can be enabled by setting `heimdali.api.authType=spnego`
- Allow a comma separated list of approvers to be notified when a workspace is requested
- Automatically pull the value for the nameservice `dfs.nameservice` from the hadoop configuration
- UI: Clicking the main logo redirects to the home screen
- Automatically create user home dir when they request a personal workspace or are added to a workspace. f3bda9be12e771474da1d816e2ca8a75ab052dc4, HEIM-243 (pull request #51)
- Added the ability to export and import workspaces via json. df0dd19065afb6d959b4e4e57a7adba644db0c63, HEIM-130 (pull request #23)
- Log all database queries when log level set to DEBUG. 53001f11089e6f0467f26d198cbcf5e4d5e7d817
- Smaller cluster service cards for optimized display on smaller resolutions. e0fd7456374088405c1fb1583ea550485772ba14
- Add Cloudera Navigator card to the UI
- Added additional property `heimdali.addition.classpath` for appending classes to the service classpath
- Added search bars in the Risk and Compliance pages. 6d9d8746d4c4d86a217183e3d5d7a03856a92f62
- Generate Markdown documentation automatically from the CSD descriptor file. a5d4d3927cc09c1e80c263bd1b0c7c6796d0725d

### Process Enhancements

- Added a new 'commit' build in Bitbucket that will build a CSD and Parcel for any commit
- Added snapshot testing for all UI components. 4542a21a867c6007708d62847c83d7e830bcf27d, Feature/HEIM-258
- Move the `control.sh` service script into the parcel for easier quick fixes and development. 6b8265722c44d384edf61dda72d277ffa55af34f, HEIM-216 (pull request #70)
- Added integration tests for provisioning
- Moved the integration tests off of the Jotunn cluster and onto Valhalla (temporarily, before moving to ephemeral clusters)
- Publish parcel shasum to parcel repo for manual parcel installs . ef80769109b40f196b4150ebbb4e26df5a5de965
- Removed all traces of the Logback module which was being used for logging during development. 70eba72be98f03ff44b4eed321cbc93ae734f086
- Created Nightly jenkins build that validates Heimdali service startup. Also able to validate arbitrary commit and test against ephemeral cluster
