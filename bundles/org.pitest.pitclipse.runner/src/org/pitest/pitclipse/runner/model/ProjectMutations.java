package org.pitest.pitclipse.pitrunner.model;

import org.pitest.pitclipse.reloc.guava.base.Function;
import org.pitest.pitclipse.reloc.guava.base.MoreObjects;
import org.pitest.pitclipse.reloc.guava.base.Objects;
import org.pitest.pitclipse.reloc.guava.collect.ImmutableList;
import org.pitest.pitclipse.reloc.guava.collect.Ordering;

import java.util.List;

import static org.pitest.pitclipse.reloc.guava.collect.Collections2.transform;

public class ProjectMutations implements Visitable, Countable {
    private final String projectName;
    private final ImmutableList<PackageMutations> packageMutations;
    private final Status status;

    private ProjectMutations(Status status, String projectName, ImmutableList<PackageMutations> packageMutations) {
        this.status = status;
        this.projectName = projectName;
        this.packageMutations = ImmutableList.copyOf(transform(packageMutations,
                new Function<PackageMutations, PackageMutations>() {
                    @Override
                    public PackageMutations apply(PackageMutations input) {
                        return input.copyOf().withProjectMutations(ProjectMutations.this).build();
                    }
                }));
    }

    @Override
    public <T> T accept(MutationsModelVisitor<T> visitor) {
        return visitor.visitProject(this);
    }

    public String getProjectName() {
        return projectName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder copyOf() {
        return builder().withPackageMutations(packageMutations).withProjectName(projectName);
    }

    public List<PackageMutations> getPackageMutations() {
        return packageMutations;
    }

    public static class Builder {
        private String projectName;
        private ImmutableList<PackageMutations> packageMutations = ImmutableList.of();
        private Status status;

        private Builder() {
        }

        public Builder withProjectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder withPackageMutations(List<PackageMutations> packages) {
            this.packageMutations = Ordering.natural().nullsLast().onResultOf(PackageName.GET)
                    .immutableSortedCopy(packages);
            return this;
        }

        public ProjectMutations build() {
            return new ProjectMutations(status, projectName, packageMutations);
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectMutations that = (ProjectMutations) o;
        return Objects.equal(projectName, that.projectName) &&
            Objects.equal(packageMutations, that.packageMutations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectName, packageMutations);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("projectName", projectName)
            .add("packageMutations", packageMutations)
            .toString();
    }

    @Override
    public long count() {
        long sum = 0L;
        for (PackageMutations packageMutation : packageMutations) {
            sum += packageMutation.count();
        }
        return sum;
    }

    public Status getStatus() {
        return status;
    }

    private enum PackageName implements Function<PackageMutations, String> {
        GET;

        @Override
        public String apply(PackageMutations input) {
            return input.getPackageName();
        }
    }

}
