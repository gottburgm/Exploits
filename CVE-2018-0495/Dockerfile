#
# https://github.com/fedorapackaging/docker-images/tree/master/F28
#
FROM fedora:28
MAINTAINER Denis Arnaud <denis.arnaud_fedora@m4x.org>

#
ENV HOME /home/build

# Update of the OS
RUN dnf -y clean all
RUN dnf -y update || echo "Issue with RPM DB, that's fine"

# Basic, convenient
RUN dnf -y install less htop net-tools which sudo keychain man wget vim || echo "Issue with installing less. That's fine"

# Fedora/CentOS/RedHat packaging
RUN dnf -y install fedora-packager keyutils rpmconf dnf-utils git-all bash-completion Lmod || echo "Issue with installing fedora-packager. That's fine"

# Specific to C++-based packages (with Python bindings)
RUN dnf -y install gcc-c++ boost-devel cmake python-devel python3-devel bzip2-devel m4 python3-numpy mpich-devel openmpi-devel || echo "Issue with installing boost-devel. That's fine"


# Create the 'build' user (for the package maintainer)
RUN adduser -m -G mock build
RUN groupadd root-users
RUN adduser -m -G root-users evil
RUN echo "build ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/build && chmod 0440 /etc/sudoers.d/build
RUN echo "root-users ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/build && chmod 0440 /etc/sudoers.d/build

# Configure SSH
RUN mkdir -p $HOME/.ssh && chmod 700 $HOME/.ssh
RUN ssh-keyscan pkgs.fedoraproject.org > $HOME/.ssh/known_hosts
RUN touch $HOME/.ssh/config
ADD files/ssh.pub $HOME/.ssh/known_hosts
RUN chmod 600 $HOME/.ssh/config $HOME/.ssh/known_hosts

RUN mkdir /root/CVE-2018-0497/

RUN dnf install glibc glibc-devel glibc-headers kernel-headers gcc -y
RUN dnf install python2 python2-devel python2-pip gdb make -y
RUN dnf install libfplll libfplll-devel python2-fpylll python2-cysignals -y
RUN dnf install mlocate openssl-1.1.0h-3.fc28.x86_64 -y
RUN dnf debuginfo-install python2-2.7.15-4.fc28.x86_64 -y

RUN pip2.7 install enum cryptography cffi
RUN pip2.7 install Cython
RUN pip2.7 install pytest
RUN pip2.7 install cysignals --no-cache-dir --force-reinstall
RUN pip2.7 install fpylll

ADD files/rohnp_poc.zip /tmp/rohnp_poc.zip
ADD files/run.sh /root/CVE-2018-0495/run.sh

WORKDIR /root/CVE-2018-0495/rohnp_poc/openssl_poc/
RUN unzip -o /tmp/rohnp_poc.zip -d /root/CVE-2018-0495/
RUN chmod +x /root/CVE-2018-0495/run.sh

ADD files/server.py /root/CVE-2018-0495/rohnp_poc/openssl_poc/

ENTRYPOINT ["/root/CVE-2018-0495/run.sh"]
