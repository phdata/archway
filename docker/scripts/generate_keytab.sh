#!/usr/bin/env expect

set adminUser [lindex $argv 0];
set adminPass [lindex $argv 1];
set project   [lindex $argv 2];
set output    [lindex $argv 3];

spawn -noecho kadmin -p $adminUser
expect "Password for $adminUser@JOTUNN.IO: " { send -- "$adminPass\r" }
expect "kadmin:  " { send -- "addprinc -randkey -x dn=\"cn=$project,ou=groups,ou=hadoop,dc=jotunn,dc=io\" $project\r" }
expect "kadmin:  " { send -- "ktadd -k $output $project\r" }
expect "kadmin:  " { send -- "quit\r" }
interact