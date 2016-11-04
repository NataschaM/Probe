package de.nata.maven.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Mojo(name = "nata")
public class NataMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Component
	private Maven maven;
	
	@Component
	private RepositorySystem repositorySystem;

	@Component
	private MavenProjectBuilder mavenProjectBuilder;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}")
	protected List<ArtifactRepository> remoteArtifactRepositories;

	@Parameter(defaultValue = "${project.pluginArtifactRepositories}")
	private List<ArtifactRepository> pluginArtifactRepositories;

	@Parameter(defaultValue = "${localRepository}")
	private ArtifactRepository localRepository;

	MavenXpp3Reader mavenReader = new MavenXpp3Reader();
	Map<String, MavenProject> allProjects;

	private MavenProject scanProject(File pom) {

		MavenProject depProject = null;
		FileReader reader = null;

		try {
			reader = new FileReader(pom);
			Model model = mavenReader.read(reader);
			model.setPomFile(pom);

			depProject = new MavenProject(model);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return depProject;
	}

	private Map<String, MavenProject> scanAllProjects(String path) {

		Map<String, MavenProject> projects = new HashMap<String, MavenProject>();

		File folder = new File(path);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				for (File file : fileEntry.listFiles()) {
					if (!file.isDirectory() && file.getName().equals("pom.xml")) {
						MavenProject proj = scanProject(file);
						projects.put(proj.getArtifactId(), proj);
					}
				}
			}
		}

		return projects;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		@SuppressWarnings("unchecked")
		List<Dependency> deps = project.getDependencies();
		String path = "/home/nata/workspace-web/Graphs";

		getLog().info("*********************************");
		getLog().info("Akt Projekt: " + project.getArtifactId());
		
		
		getLog().info(project.getScm().getUrl());
		getLog().info("Führe versions für " + project.getArtifactId() + " aus.");
		
		List<String> goals = new ArrayList<String>();
		goals.add("versions:use-next-versions -Dincludes=de.ruv.*,de.nata.*");
		goals.add("versions:commit");
		goals.add("scm:checkin -Dmessage=\"checkin\"");

		MavenExecutionRequest req = new DefaultMavenExecutionRequest();
		req = req.setPom(project.getModel().getPomFile());
		req = req.setBaseDirectory(project.getModel().getPomFile().getParentFile());
		req = req.setGoals(goals);
		req = req.setProxies(session.getSettings().getProxies());
		req = req.setMirrors(session.getSettings().getMirrors());
		req = req.setLocalRepository(localRepository);
		req = req.setRemoteRepositories(remoteArtifactRepositories);
		req = req.setPluginArtifactRepositories(pluginArtifactRepositories);
		MavenExecutionResult result = maven.execute(req);

		getLog().info("*********************************");
		
//		for (Dependency dep : deps) {
//			getLog().info(dep.getArtifactId() + " " + dep.getVersion());
//			if (dep.getVersion().endsWith("SNAPSHOT")) {
//
//				if (allProjects == null) {
//					allProjects = scanAllProjects(path);
//				}
//				
//				getLog().info("*********************************");
//				getLog().info("Führe nata-plugin für " + dep.getArtifactId() + " aus.");
//				
//				MavenProject actProject = allProjects.get(dep.getArtifactId());
//				getLog().info("Snapshot-Project: " + dep.getArtifactId());
//
//				goals = new ArrayList<String>();
//				goals.add("de.nata.maven.plugin:nata-maven-plugin:nata");
//
//				req = new DefaultMavenExecutionRequest();
//				req.setPom(actProject.getModel().getPomFile());
//				req.setBaseDirectory(actProject.getModel().getPomFile().getParentFile());
//				req.setGoals(goals);
//				req.setProxies(session.getSettings().getProxies());
//				req.setMirrors(session.getSettings().getMirrors());
//				req.setLocalRepository(localRepository);
//				req.setRemoteRepositories(remoteArtifactRepositories);
//				req.setPluginArtifactRepositories(pluginArtifactRepositories);
//				result = maven.execute(req);
//				
//				getLog().info(result.getProject().getVersion());
//				getLog().info("*********************************");
//
//			}
//		}
//		
//		getLog().info("*********************************");
//		getLog().info("Führe versions für " + project.getArtifactId() + " aus.");
//		
//		goals = new ArrayList<String>();
//		goals.add("versions:use-next-versions -Dincludes=de.ruv.*,de.nata.*");
//		goals.add("versions:commit");
//		goals.add("scm:checkin -Dmessage=\"checkin\"");
//
//		req = new DefaultMavenExecutionRequest();
//		req.setPom(project.getModel().getPomFile());
//		req.setBaseDirectory(project.getModel().getPomFile().getParentFile());
//		req.setGoals(goals);
//		req.setProxies(session.getSettings().getProxies());
//		req.setMirrors(session.getSettings().getMirrors());
//		req.setLocalRepository(localRepository);
//		req.setRemoteRepositories(remoteArtifactRepositories);
//		req.setPluginArtifactRepositories(pluginArtifactRepositories);
//		result = maven.execute(req);
//
//		getLog().info("*********************************");
//		
//		getLog().info("*********************************");
//		getLog().info("Führe release-plugin für " + project.getArtifactId() + " aus.");
//		List<String> actGoals = new ArrayList<String>();
//		actGoals.add("release:clean");
//		actGoals.add("release:prepare");
//		actGoals.add("release:perform -DreleaseVersion=${releaseVersion} -DdevelopmentVersion=${developmentVersion}");
//		actGoals.add("deploy");
//
//		req = new DefaultMavenExecutionRequest();
//		req.setPom(project.getModel().getPomFile());
//		req.setBaseDirectory(project.getModel().getPomFile().getParentFile());
//		req.setGoals(actGoals);
//		req.setProxies(session.getSettings().getProxies());
//		req.setMirrors(session.getSettings().getMirrors());
//		req.setLocalRepository(localRepository);
//		req.setRemoteRepositories(remoteArtifactRepositories);
//		req.setPluginArtifactRepositories(pluginArtifactRepositories);
//		result = maven.execute(req);
//		
//		getLog().info(result.getProject().getVersion());
//		
//		getLog().info("*********************************");
	}

}
