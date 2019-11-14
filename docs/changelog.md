## 2.2.4

- Changes scalate workdir to workaround a 'resource not found' exception when scalate pre-compiled classes get cleaned up. 7460c0e9feee33be52403598f8892d27af549c0a
- Log additional information when adding or removing a user from a workspace . 3464f3189d80fe5a9d4f26ade37a1ab3bd133134
- Render workspace example when a user workspace is created without a yarn queue. cb2f42f197bafd6ea1a19d4e4f718c6b800080a7, 6282f0f24db03dad861dced0ad21a93b91e0794e
- Revert unique workspace names. This causes issues with creating similar named workspaces in different environments within a cluster.
- Validate member id when adding a user, only showing 'OK' button when a member has been picked. 7f141d903222cdbfa53d6dab10bece15251fe646

## 2.2.3

- Fix a condition where the provisioningLDAPClient was used for a lookup operation,
  causing issues at clients where the operations need to be separated

## 2.2.2

- Fix bug where tabs under the workspace view didn't show correctly for ops users. 9a9934c8ef3de845b72b11ddc66a8afee173267e
- Only show 'manage' tabs in workspaces to the ops user. 16ae15fb06bcc2e2682037a546cc664c77d70bdf

## 2.2.1

- Fixes bug where some user workspaces would show up in the operations or risk/compliance tabs.
  Workspace provisioning was not affected by this bug. 796ec84c4e1b1719040123625842924773f23ea0

## 2.2.0

### Enhancements

- Add optional configuration 'Archway Authorization Group DN'. If this is set, only users in this group
  will be allowed to log into Archway. 4390c028ef887e312b0c99658212112e57ad2e89
- Customizable links. Links can be customized by the operations users in the 'manage' tab of the application.
  Links can be organized into groups, updated, or deleted. Automatic resolution of links (like Hue and Yarn RM)
  has been removed because much of the time it would have to be overridden anyways. Automatic link resolution may be added
  at a later time. 444d20530b7fd9877c20170bb6cc376b05014203
- Yarn resource pool cores/memory can now be updated by operations users with the 'update' button below a resource pool
  info box. 2a502eb4726cde920d4e6b40da63556d33a2eda7
- Disk quota is configurable in the UI by the operations users. a717c13771f46b478cd5e8f9bd4c12cf9b8c9b84
- Set the X-Frame-Options to DENY to disallow any type of clickjacking attacks. 95f2bd06749df630b5068b7e9e68a53a54e3f5bf

### Bug Fixes

- Fix a bug where users weren't removed from Sentry AD groups when removed from a workspace. 79f85ff3f15ca376967c35c986b8b5211b868c04
- Removed the concept of a 'superuser'. The superuser had extra permissions if they were in both 'operations' and
  'compliance' groups. The permissions have been transferred to the operations users because they users need the ability
  to view all workspaces and do administrative actions like provision or deprovision workspaces. 198863e16175581a64b60cb71a9119b5ef412c40
- Fix a bug where CM would show duplicate port errors, even when there was no real port conflict. 562d897bff76121bbeb13e3aaaac1bcc90201994
- Fix a bug where some elements failed to render when fetching hive tables failed in a workspace. 078cb02a3fdcd0593a8b062f011df18911a430bc

## 2.1.3

- Don't show cluster status, it is difficult for users to interpret. 72f134b3cb15fe8b99a02cf600780e1b56bd393e

## 2.1.2

### Bug Fixes

- Fix change-owner functionality to properly add new owner to workspace. 66484e23d9b87cb60de6495999fbbdcf5cca28ee

## 2.1.1

### Bug Fixes

- Fix a bug where users who were not in both risk and ops groups were denied permission to see workspaces.

## 2.1.0

### Enhancements

- Restyled workspace 'Wecome' email.
- Add a workspace 'hide' button in the workspace 'manage' section. This is a replacement for the crazy deletion scripts that were buggy. The hide button does a logical delete of the workspace so it is ignored in workspace listings. It deletes no data. A workspace can optionally be 'deprovisioned' before it is hidden to remove resources like AD groups and hive databases. 509c24186de7747dd63ed3ce864f76b1524c18d3
- Ability to change workspace owners in the 'manage' section.
- Send the log file back to phData using the 'support bundle' role command in the server role. 090fe04943319dfdc87f0c773df6f1e86357702e
- AD Group synchronization. Archway keeps track of Active directory groups in its own database for performance reasons.
  Previously, if a user was removed from AD outside of Archway, the user would still show up as a member of a workspace in
  the Archway UI. Now, if a user is removed from AD or an Archway AD group they will be removed from the Archway database and
  UI. In addition, if a user is added to an Archway managed AD group, they will automatically be added to the Archway database and
  UI. Synchronizing groups runs as a scheduled job every 2 hours by default. d814a3d718ec8ee1f0a25d995f4bbf2c26af4c5b
- Mask all passwords in the UI. It was possible for passwords to be printed to the UI during debug logging, this has been fixed and
  now when a password would have been displayed '**\*\*\***' will be displayed in its place. 839e118c2e7f4ef29fa3ee20beaa0d3dbdfd6bf8
- The 'Add Member' button in a workspace is now greyed out until the workspace is provisioned. 1e4c66e6768fbbde33eb7806be9c176d6ef79532
- Modify the 'MEMBER ID SEARCH' box to show the user id first in the auto-completeion. 7fb08124eb8b899883a4c6f9c35c57ec4d7850d0
- Option added for 'smtps' in addition to SSL. 2ba2e7f359f9f7bc963f8401b3d0ef3e1d06c4f8

## Bug fixes

- Make LDAP lookup bindings optional. Previously functionality was added to automatically fall back to the provisioning bindings
  if no lookup bindings were provided, but CM was still asking for lookup bindings to be provided. This has been corrected. 46cfd972a3a1244b26375aeac0f610989efe9d59
- Fix a bad logging message where the 'from' address was being logged as the 'to' address in email notifications. ad8a2523c06aa1fd572ee2859d97c87fd0ba5ec5
- Reflect actual number of partitions in the UI when the minimum partition (3) is used. cac1b09adfab6e998a39e8520c32ea9eef1e2c21
- Clean up all temporary tables after they are used (heimdali_temp). 2f776f16445298b12b308ce822f4ba98dadfa939
- '/clusters' endpoint now requires authentication. 2a6117fb471c9020dae743847b2e000b0e35087c

## 2.0.0

### Enhancements

- Rename to Archway. ef7c086e18f14af03cb5b6e2bd999b481f02aafd
- Add a spinner in the user workspace member add search bar. 6ec147f78cf81e236c7bf7ae5e1d9741bb8e8ca2
- Handle database creation/updates automatically on application start. There is no longer a need to download
  any special packages (flyway) or run a role command. 11272d43c4a11a105d4830a5309616d9f2274570
- Validate all templates, including custom templates, on startup. dca69a9715f02487be6cda64b66d9e55553c50c0

### Bug Fixes

- Users are now blocked from viewing workspaces that they aren't a member of. 2f07a16ec01e8b38fe519edd27f75a2ae3fcda64

### Breaking Changes

With the renaming, Archway 2.0.0 introduces a new kerberos principal name, so permissions will need to be added to the cluster for the 'archway' user
in place of the 'heimdali_api' user. See the Archway install doc for details

Templates will also need to be modified, replacing the package names at the head of the file 'com.heimdali' with 'io.phdata'.

All data created by Archway, workspaces, and the 'heimdali' database are fully compatible with Archway 2.0

## 1.7.2

- Fix bug where the UI would fail if a user didn't have a kafka topic. d2851bc290263c6d5d11a75e83b6bb9fc146b550
- Added full http client request logging when 'TRACE' logging is enabled. 42958024ebe90483a335911d2584b7c90347995f

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
