package com.mageddo.repo

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.language.java.artifact.JavadocArtifact

/**
 * @author elvis
 * @version $Revision: $<br/>
 *          $Id: $
 * @since 6/2/16 4:45 PM
 */
public class RepoBuilderTask extends DefaultTask {

	/**
	 * The specified local maven repository
	 */
	def mavenRepoFolder;

	public RepoBuilderTask() {
		this.description = "save all remote dependencies to specified maven repository"
	}

	File getDestination() {
		project.file(mavenRepoFolder)
	}

	@TaskAction
	void buildRepo(){

		logger.info("M=buildRepo, msg=hello");

		if(!getDestination()){
			throw new AssertionError("please pass a folder on mavenRepoFolder variable");
		}

		def componentIds = project.configurations.compile.incoming.resolutionResult.allDependencies.collect { it.selected.id }
		def result = project.dependencies.createArtifactResolutionQuery()
				.forComponents(componentIds)
				.withArtifacts(JvmLibrary, SourcesArtifact, JavadocArtifact)
				.execute()

		for (component in result.resolvedComponents) {

			component.getArtifacts(SourcesArtifact).each {

				def thefile = it.file;
				if(it.file.absolutePath.startsWith(mavenRepoFolder.absolutePath)){
					logger.info("M=buildRepo, status=alreadyOnEmbedRepo");
					return;
				}
				def group = component.id.properties.get("group");
				def module = component.id.properties.get("module");
//					def displayName = component.id.properties.get("displayName");
				def version = component.id.properties.get("version");

				logger.info("M=buildRepo, artifactFile=${thefile}, component=${component.id.properties}");

				def gradleCacheFolder = getGradleCacheFolder(thefile, group);
				final File destPath = toLocalDependencyMavenFolder(group, module, version);
				destPath.mkdirs();

				logger.info("M=buildRepo, destPath=${destPath}")
				logger.info("M=buildRepo, gradle-cache-folder=${gradleCacheFolder}")

				copyOnlyFilesToPath(thefile.getParentFile().getParentFile(), destPath);

				def readPoms = [:], times = 1;
				while(true) {

					def xfiles = project.fileTree(getDestination()) {
						include "**/*.pom"
					}
					boolean hasSomeoneProcessed = false;
					xfiles.each {

						def list = new XmlParser().parse(it);
						def thisId = toId(list.groupId.text(), list.artifactId.text(), list.version.text());

						if(readPoms.containsKey(thisId)){
							println("cached!");
							return ;
						}
						println("processing!");
						hasSomeoneProcessed = true;
						readPoms.put(thisId, null);

						def parentGroup = list.parent.groupId.text();
						def parentModule = list.parent.artifactId.text();
						def parentVersion = list.parent.version.text();

						if (!parentGroup) {
							return;
						}

						def mavenFolder = toLocalDependencyMavenFolder(parentGroup, parentModule, parentVersion);
						mavenFolder.mkdirs();

						def dependencyFolder = new File(
								"${parentGroup}/${parentModule}/${parentVersion}", new File(gradleCacheFolder)
						);
						copyOnlyFilesToPath(dependencyFolder, mavenFolder);

						logger.info("M=buildRepo, parentGroup=${list.parent.groupId.text()}, artifactId=${list.parent.artifactId.text()}, version=${list.parent.version.text()}")
					}
					logger.info("M=buildRepo, level=${times++}")
					if(!hasSomeoneProcessed){
						break;
					}
				}
			}
		}
	}

	def String getGradleCacheFolder(dependencyFile, groupId){
		def absolutePath = dependencyFile.absolutePath
		def index = absolutePath.indexOf(groupId);

		logger.info(
				"M=getGradleCacheFolder, dependencyFile={}, groupId={}, index={}",
				dependencyFile, groupId, index
		);

		return absolutePath.substring(0, index);
	}

	def String toId(group, module, version){
		return "${group}:${module}:${version}";
	}

	def copyOnlyFilesToPath(fromPath, toPath){

		logger.info("M=copyOnlyFilesToPath, fromPath=${fromPath}, toPath=${toPath}")

		ant.copy(
				todir: toPath,
				failonerror: false,
				flatten: true
		){
			fileset(dir: fromPath){
				include("name": "**/*.jar")
				include("name": "**/*.pom")
			}
		}
	}

	def toLocalDependencyMavenFolder(group, module, version){
		String base = "${mavenRepoFolder.absolutePath}/${group}/${module}".replaceAll("\\.", "/").replaceAll(":", "/");
		return new File("${base}/${version}")
	}
}
