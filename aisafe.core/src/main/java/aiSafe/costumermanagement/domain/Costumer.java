package aiSafe.costumermanagement.domain;

import aiSafe.usermanagement.domain.Email;

/**
 * Interface design to represent a costumer in the System.
 * @author 2DE - Group 3
 * @version 1.0
 */
public interface Costumer {
    String customerReference();

    String displayName();

    boolean hasCollaborator(Email email);

    void addMembership(Membership membership);
}
