jboss-service-VDFDeploymentScanner.xml

   drop in replacement for conf/jboss-service.xml that replaces
   URLDeploymentScanner with a combination of VDFLoaderService,
   loading the default
   org.jboss.deployers.vdf.plugins.basic.BasicComponentFactory
   injected to a VDFDeploymentScanner
   (a URLDeploymentScanner re-written to work over VDFComponents)

maindeployer-adapter-service.xml

   combination of VDFLoaderServer + MainDeployerAdapter + URLDeploymentScanner
   that can load new MainDeployer inside the existing server. This
   one will look for deployments in the ./deploy2 directory.