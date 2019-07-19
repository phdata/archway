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
