#!/bin/sh

#####################################################################################
#                                                                                   #
# * Description :                                                                   #
#       This is the installation script for the script                              #
#       fingerprint.pl which install all the requirements needed                    #
#       to use it .                                                                 #
#                                                                                   #
#                                                                                   #
# * Date : 04.05.2018                                                               #
#                                                                                   #
# * Author :                                                                        #
#                                                                                   #
# *  NOTES :                                                                        #
#    - This File Was Generated Automatically And May Contains Bugs                  #
#                                                                                   #
#                                                                                   #
#####################################################################################

BASE=$(pwd)
PKG_MANAGER=""
PKG_DEV="devel"

if [ -e "/bin/dnf" ] ; then
    PKG_MANAGER="dnf"
else
    if [ -e "/bin/apt" ] ; then
        PKG_MANAGER="apt-get"
        PKG_DEV="dev"
    fi
fi

echo "[*] Using Package Manager : $PKG_MANAGER"
echo "[*] Installing Depencies"
sudo $PKG_MANAGER install -y perl perl-$PKG_DEV
 
echo "[*] Installing Perl Dependencies ..."
cpan -f -T -i Getopt::Long
cpan -f -T -i DateTime
cpan -f -T -i URI::URL
cpan -f -T -i HTTP::Request
cpan -f -T -i HTTP::Cookies
cpan -f -T -i HTTP::Proxy
cpan -f -T -i HTTP::Daemon
cpan -f -T -i Time::HiRes
cpan -f -T -i Term::ANSIColor
cpan -f -T -i Data::Dump
cpan -f -T -i Data::Validate::URI
cpan -f -T -i LWP::UserAgent
cpan -f -T -i LWP::Protocol::http
cpan -f -T -i LWP::Protocol::https
cpan -f -T -i MIME::Base32
cpan -f -T -i MIME::Base64
cpan -f -T -i HTML::Entities
cpan -f -T -i HTTP::Response
cpan -f -T -i Scalar::Util
cpan -f -T -i Digest::MD5
cpan -f -T -i JSON
cpan -f -T -i JSON::Parse

echo "[+] Done."
