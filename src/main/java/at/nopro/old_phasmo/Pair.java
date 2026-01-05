package at.nopro.old_phasmo;

// https://stackoverflow.com/a/521235
public record Pair<L, R>(L left, R right) {
    public Pair {
        assert left != null;
        assert right != null;

    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!( o instanceof Pair pairo )) return false;
        return this.left.equals(pairo.left()) &&
                this.right.equals(pairo.right());
    }

}
