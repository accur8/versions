package io.accur8.neodeploy


import a8.shared.SharedImports._
import io.accur8.neodeploy.resolvedmodel.{ResolvedRSnapshotClient, ResolvedRSnapshotServer}
import PredefAssist._

object RSnapshotConfig {

  def serverConfigForClient(serverConfig: ResolvedRSnapshotServer, client: ResolvedRSnapshotClient): String = {

    z"""
#################################################
# rsnapshot.conf - rsnapshot configuration file #
#################################################
#                                               #
# PLEASE BE AWARE OF THE FOLLOWING RULE:        #
#                                               #
# This file requires tabs between elements      #
#                                               #
#################################################

#######################
# CONFIG FILE VERSION #
#######################

config_version	1.2

###########################
# SNAPSHOT ROOT DIRECTORY #
###########################

# All snapshots will be stored under this root directory.
#
snapshot_root	${serverConfig.descriptor.rsnapshotRootDir.unresolvedDirectory.subdir(client.server.name.value)}

# If no_create_root is enabled, rsnapshot will not automatically create the
# snapshot_root directory. This is particularly useful if you are backing
# up to removable media, such as a FireWire or USB drive.
#
#no_create_root	1

#################################
# EXTERNAL PROGRAM DEPENDENCIES #
#################################

# LINUX USERS:   Be sure to uncomment "cmd_cp". This gives you extra features.
# EVERYONE ELSE: Leave "cmd_cp" commented out for compatibility.
#
# See the README file or the man page for more details.
#
cmd_cp		/bin/cp

# uncomment this to use the rm program instead of the built-in perl routine.
#
cmd_rm		/bin/rm

# rsync must be enabled for anything to work. This is the only command that
# must be enabled.
#
cmd_rsync	/usr/bin/rsync

# Uncomment this to enable remote ssh backups over rsync.
#
cmd_ssh	/usr/bin/ssh

# Comment this out to disable syslog support.
#
cmd_logger	/usr/bin/logger

# Uncomment this to specify the path to "du" for disk usage checks.
# If you have an older version of "du", you may also want to check the
# "du_args" parameter below.
#
cmd_du		/usr/bin/du

# Uncomment this to specify the path to rsnapshot-diff.
#
#cmd_rsnapshot_diff	/usr/bin/rsnapshot-diff

#########################################
#     BACKUP LEVELS / INTERVALS         #
# Must be unique and in ascending order #
# e.g. alpha, beta, gamma, etc.         #
#########################################

retain	hourly	24
retain	daily	7
retain	weekly	5
retain	monthly	4
#retain	delta	3

############################################
#              GLOBAL OPTIONS              #
# All are optional, with sensible defaults #
############################################

# Verbose level, 1 through 5.
# 1     Quiet           Print fatal errors only
# 2     Default         Print errors and warnings only
# 3     Verbose         Show equivalent shell commands being executed
# 4     Extra Verbose   Show extra verbose information
# 5     Debug mode      Everything
#
verbose		3

# Same as "verbose" above, but controls the amount of data sent to the
# logfile, if one is being used. The default is 3.
# If you want the rsync output, you have to set it to 4
#
loglevel	3

# If you enable this, data will be written to the file you specify. The
# amount of data written is controlled by the "loglevel" parameter.
#
logfile	/var/log/rsnapshot-${client.server.name}.log

# If enabled, rsnapshot will write a lockfile to prevent two instances
# from running simultaneously (and messing up the snapshot_root).
# If you enable this, make sure the lockfile directory is not world
# writable. Otherwise anyone can prevent the program from running.
#
lockfile	/var/run/rsnapshot-${client.server.name}.pid

# By default, rsnapshot check lockfile, check if PID is running
# and if not, consider lockfile as stale, then start
# Enabling this stop rsnapshot if PID in lockfile is not running
#
#stop_on_stale_lockfile		0

# Default rsync args. All rsync commands have at least these options set.
#
#rsync_short_args	-a
#rsync_long_args	--delete --numeric-ids --relative --delete-excluded

# ssh has no args passed by default, but you can specify some here.
#
#ssh_args	-p 22

# Default arguments for the "du" program (for disk space reporting).
# The GNU version of "du" is preferred. See the man page for more details.
# If your version of "du" doesn't support the -h flag, try -k flag instead.
#
#du_args	-csh

# If this is enabled, rsync won't span filesystem partitions within a
# backup point. This essentially passes the -x option to rsync.
# The default is 0 (off).
#
one_fs		1

# The include and exclude parameters, if enabled, simply get passed directly
# to rsync. If you have multiple include/exclude patterns, put each one on a
# separate line. Please look up the --include and --exclude options in the
# rsync man page for more details on how to specify file name patterns.
#
${client.resolvedIncludeExcludeLines}


# The include_file and exclude_file parameters, if enabled, simply get
# passed directly to rsync. Please look up the --include-from and
# --exclude-from options in the rsync man page for more details.
#
#include_file	/path/to/include/file
#exclude_file	/path/to/exclude/file

# If your version of rsync supports --link-dest, consider enabling this.
# This is the best way to support special files (FIFOs, etc) cross-platform.
# The default is 0 (off).
#
link_dest	1

# When sync_first is enabled, it changes the default behaviour of rsnapshot.
# Normally, when rsnapshot is called with its lowest interval
# (i.e.: "rsnapshot alpha"), it will sync files AND rotate the lowest
# intervals. With sync_first enabled, "rsnapshot sync" handles the file sync,
# and all interval calls simply rotate files. See the man page for more
# details. The default is 0 (off).
#
sync_first	1

# If enabled, rsnapshot will move the oldest directory for each interval
# to [interval_name].delete, then it will remove the lockfile and delete
# that directory just before it exits. The default is 0 (off).
#
#use_lazy_deletes	0

# Number of rsync re-tries. If you experience any network problems or
# network card issues that tend to cause ssh to fail with errors like
# "Corrupted MAC on input", for example, set this to a non-zero value
# to have the rsync operation re-tried.
#
rsync_numtries 3


###############################
### BACKUP POINTS / SCRIPTS ###
###############################

${client.resolvedBackupLines}


"""

  }

}
