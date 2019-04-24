#!/bin/bash

TESTING_SERVER_MODS="--enable-info --enable-ssl --enable-cgi --enable-dav"

if [ "$1" != "" ]
then
    HTTPD_VERSION="$1"
    INSTALL_DIRECTORY="/usr/local"
    PRODUCT="httpd-$HTTPD_VERSION"
    DOWNLOAD_URL="https://archive.apache.org/dist/httpd/$PRODUCT.tar.gz"
    LOGFILE="/tmp/$PRODUCT-install.log"
    
    if [ "$2" != "" ]
    then
        INSTALL_DIRECTORY="$2"
    else
        INSTALL_DIRECTORY="$INSTALL_DIRECTORY/$PRODUCT"
    fi
    
    if [ -e "$INSTALL_DIRECTORY" ]
    then
        echo "[!] WARNING : $INSTALL_DIRECTORY Will Be Overwritten By New Installation"
        echo "[*] Are You Sure That You Want To Install HTTPd $HTTPD_VERSION In : $INSTALL_DIRECTORY ? (y,n) : "
        read CONFIRM
        if ![ "$CONFIRM" == "Y" ] && ![ "$CONFRIM" == "y" ] && ![ "$CONFIRM" == "yes" ] 
        then
            echo "[*] Aborting ..."
            exit
        fi
    fi
    
    echo "###################################### Summary ######################################"
    echo ""
    echo "          Product Name: $PRODUCT"
    echo "               Version: $HTTPD_VERSION"
    echo "         Download From: $DOWNLOAD_URL"
    echo "     Install Directory: $INSTALL_DIRECTORY"
    echo ""
    echo "#####################################################################################"
    
    echo "[*] Downloading $PRODUCT Sources ..."
    wget $DOWNLOAD_URL -O /tmp/$PRODUCT.tar.gz >> $LOGFILE 2>&1
    configuration_parameters="--with-mpm=worker --prefix=\"${INSTALL_DIRECTORY}/\""
    echo "[*] Extracting Files In : /tmp/$PRODUCT ..."
    tar -x -z -f /tmp/$PRODUCT.tar.gz -C /tmp
    sudo rm -rf /tmp/$PRODUCT.tar.gz
    cd /tmp/$PRODUCT
    if [ ! -f "/usr/bin/apr-1-config" ]
    then
        sudo svn co http://svn.apache.org/repos/asf/apr/apr/trunk srclib/apr >> $LOGFILE 2>&1
        cd srclib/apr

        echo "[*] Building APR ..."
        sudo ./buildconf >> $LOGFILE 2>&1
        sudo ./configure >> $LOGFILE 2>&1
        sudo make clean
        sudo make >> $LOGFILE 2>&1
    
        echo "[*] Installing APR ..."
        sudo make install >> $LOGFILE 2>&1
        configuration_parameters="${configuration_parameters} --with-included-apr"
        cd ../../
    else
       configuration_parameters="${configuration_parameters} --with-apr=/usr/bin/apr-1-config"
    fi
    echo "[*] Building $PRODUCT ..."
    sudo ./buildconf >> $LOGFILE 2>&1
    eval "sudo ./configure ${configuration_parameters}" >> $LOGFILE 2>&1
    sudo make clean
    sudo make >> $LOGFILE 2>&1
    
    echo "[*] Installing $PRODUCT Files ..."
    sudo make install >> $LOGFILE 2>&1
    
    BUILD_INFO=$(sudo $INSTALL_DIRECTORY/bin/apachectl -v)
    
    if [ "$BUILD_INFO" != "" ]
    then
        echo "[+] Installation Done ."
        echo ""
        echo "$BUILD_INFO"
    else
        echo "[-] ERROR : Installation Failed"
        echo ""
        echo " -> Please Check The Installation Log File : $LOGFILE"
    fi
else
    echo "===================================[ Usage ]==================================="
    echo "  Arguments :"
    echo "        $0 <HTTPD_VERSION>"
    echo "        $0 <HTTPD_VERSION> [APCHE_INSTALL_DIRECTORY]"
    echo ""
    echo "  Exemple :"
    echo "        $0 2.4.29 /usr/local/httpd-2.4.29"
    echo ""
    echo "==============================================================================="
fi
