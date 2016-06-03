package com.mageddo.repo
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author elvis
 * @since 6/2/16 3:45 PM
 * @version $Revision: $<br/>
 *          $Id: $
 *
 */
public class EmbedMavenRepoPlugin implements Plugin<Project> {


	@Override
	void apply(Project project) {

		project.ext.RepoBuilder = RepoBuilderTask.class

		project.tasks.withType(RepoBuilderTask.class).all { task ->
			logger.info('Applying docker defaults to task {}', task.name);

		}

	}
}