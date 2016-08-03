package edu.wpi.grip.core;

import edu.wpi.grip.core.util.Icon;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An interface describing how an operation should be displayed in the {@link Palette} to the user.
 */
@Immutable
public class OperationDescription {

  private final String name;
  private final String summary;
  private final Category category;
  private final Icon icon;
  private final ImmutableSet<String> aliases;

  /**
   * Private constructor - use {@link #builder} to instantiate this class.
   */
  private OperationDescription(String name,
                               String summary,
                               Category category,
                               Icon icon,
                               Set<String> aliases) {
    this.name = checkNotNull(name, "Name cannot be null");
    this.summary = checkNotNull(summary, "Summary cannot be null");
    this.category = checkNotNull(category, "Category cannot be null");
    this.icon = icon; // This is allowed to be null
    this.aliases = ImmutableSet.copyOf(checkNotNull(aliases, "Aliases cannot be null"));
  }

  /**
   * Creates a new {@link Builder} instance to create a new {@code OperationDescription} object. The
   * created descriptor has a default category of {@link Category#MISCELLANEOUS MISCELLANEOUS} and
   * no icon; use the {@link Builder#category(Category) .category()} and {@link Builder#icon(Icon)
   * .icon()} methods to override the default values.
   */
  public static Builder builder() {
    return new Builder()
        .category(Category.MISCELLANEOUS)
        .icon(null);
  }

  /**
   * @return The unique user-facing name of the operation, such as "Gaussian Blur".
   */
  public String name() {
    return name;
  }

  /**
   * @return A summary of the operation.
   */
  public String summary() {
    return summary;
  }

  /**
   * @return What category the operation falls under.  This is used to organize them in the GUI.
   */
  public Category category() {
    return category;
  }

  /**
   * An {@link InputStream} of a 128x128 image to show the user as a representation of the
   * operation.
   *
   * @return The icon to be displayed.
   */
  public Optional<InputStream> icon() {
    return Optional.ofNullable(icon).map(Icon::getStream);
  }

  /**
   * This is used to preserve compatibility with old versions of GRIP if the operation name
   * changes.
   *
   * @return Any old unique user-facing names of the operation.
   */
  public ImmutableSet<String> aliases() {
    return aliases;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OperationDescription)) {
      return false;
    }

    OperationDescription that = (OperationDescription) o;

    return Objects.equals(name, that.name)
        && Objects.equals(summary, that.summary)
        && Objects.equals(category, that.category)
        && Objects.equals(icon, that.icon)
        && Objects.equals(aliases, that.aliases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, summary, category, icon, aliases);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("summary", summary)
        .add("aliases", aliases)
        .add("category", category)
        .toString();
  }

  /**
   * The categories that entries can be in.
   */
  public enum Category {
    IMAGE_PROCESSING,
    FEATURE_DETECTION,
    NETWORK,
    LOGICAL,
    OPENCV,
    MISCELLANEOUS,
  }

  /**
   * Builder class for {@code OperationDescription}.
   */
  public static final class Builder {
    private String name;
    private String summary = "PLEASE PROVIDE A DESCRIPTION TO THE OPERATION DESCRIPTION!";
    private Category category;
    private Icon icon;
    private ImmutableSet<String> aliases = ImmutableSet.of(); // default to empty Set to
    // avoid NPE if not assigned

    /**
     * Private constructor; use {@link OperationDescription#builder()} to create a builder.
     */
    private Builder() {
    }

    /**
     * Sets the name.
     */
    public Builder name(String name) {
      this.name = checkNotNull(name);
      return this;
    }

    /**
     * Sets the summary.
     */
    public Builder summary(String summary) {
      this.summary = checkNotNull(summary);
      return this;
    }

    /**
     * Sets the category.
     */
    public Builder category(Category category) {
      this.category = checkNotNull(category);
      return this;
    }

    /**
     * Sets the icon.
     */
    public Builder icon(Icon icon) {
      this.icon = icon;
      return this;
    }

    /**
     * Sets the aliases.
     */
    public Builder aliases(String... aliases) {
      this.aliases = ImmutableSet.copyOf(checkNotNull(aliases));
      return this;
    }

    /**
     * Builds a new {@code OperationDescription}.
     */
    public OperationDescription build() {
      return new OperationDescription(
          name,
          summary,
          category,
          icon,
          aliases);
    }
  }

}
