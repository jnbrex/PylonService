package com.pylon.pylonservice.model.requests.shard;

import com.pylon.pylonservice.model.domain.ShardAdditionalLink;
import com.pylon.pylonservice.model.domain.ShardRule;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateShardRequestTests extends ShardRequestTests {
    private static final String VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    private static final String VALID_SHARD_FEATURED_IMAGE_FILENAME_JPG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    private static final String VALID_SHARD_FEATURED_IMAGE_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    private static final String VALID_SHARD_FEATURED_IMAGE_LINK = "http://whatever.com";
    private static final Collection<ShardRule> VALID_SHARD_RULES = Stream.of(
        new ShardRule("rule 1", "this is the rule 1 description"),
        new ShardRule("this is rule 2", "asjdfouwehfiujihugyuvbhjh8y97g7%^&(*tg7h08")
    ).collect(Collectors.toList());
    private static final Collection<ShardRule> VALID_SHARD_RULES_EMPTY = Collections.emptyList();
    private static final Collection<ShardAdditionalLink> VALID_SHARD_ADDITIONAL_LINKS = Stream.of(
        new ShardAdditionalLink("title here", "aweregf"),
        new ShardAdditionalLink("asjdfouwehfiujihugyuvbh", "http://whatever.com")
    ).collect(Collectors.toList());
    private static final Collection<ShardAdditionalLink> VALID_SHARD_ADDITIONAL_LINKS_EMPTY = Collections.emptyList();

    private static final String INVALID_SHARD_FEATURED_IMAGE_FILENAME_NON_UUID = "not a uuid";
    private static final String INVALID_SHARD_FEATURED_IMAGE_FILENAME_BAD_EXTENSION
        = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpeg";
    private static final String INVALID_SHARD_FEATURED_IMAGE_LINK_TOO_LONG = "b".repeat(501);
    private static final Collection<ShardRule> INVALID_SHARD_RULES_RULE_NAME_TOO_LONG = Stream.of(
        new ShardRule("A".repeat(41), "this is the rule 1 description"),
        new ShardRule("this is rule 2", "asjdfouwehfiujihugyuvbhjh8y97g7%^&(*tg7h08")
    ).collect(Collectors.toList());
    private static final Collection<ShardRule> INVALID_SHARD_RULES_RULE_DESCRIPTION_TOO_LONG = Stream.of(
        new ShardRule("rule 1", "this is the rule 1 description"),
        new ShardRule("this is rule 2", "A".repeat(141))
    ).collect(Collectors.toList());
    private static final Collection<ShardRule> INVALID_SHARD_RULES_NULL = null;
    private static final Collection<ShardAdditionalLink> INVALID_SHARD_ADDITIONAL_LINKS_URL_TOO_LONG = Stream.of(
        new ShardAdditionalLink("title here", "http://whatever.com"),
        new ShardAdditionalLink("asjdfouwehfiujihugyuvbhjh8y97g7%^&(*tg7h08", "a".repeat(501))
    ).collect(Collectors.toList());
    private static final Collection<ShardAdditionalLink> INVALID_SHARD_ADDITIONAL_LINKS_TITLE_TOO_LONG = Stream.of(
        new ShardAdditionalLink("a".repeat(31), "http://whatever.com"),
        new ShardAdditionalLink("ASJLKAAKL", "asjdfouwehfiujihugyuvbhjh8y97g7%^&(*tg7h08")
    ).collect(Collectors.toList());
    private static final Collection<ShardAdditionalLink> INVALID_SHARD_ADDITIONAL_LINKS_NULL = null;

    @DataProvider
    private Object[][] provideValidUpdateShardRequests() {
        return new Object[][] {
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_GIF,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_JPG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_GIF,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_JPG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_GIF,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    "",
                    "",
                    "",
                    "",
                    VALID_INHERITED_SHARD_NAMES_EMPTY,
                    VALID_INHERITED_USERS_EMPTY,
                    "",
                    "",
                    VALID_SHARD_RULES_EMPTY,
                    VALID_SHARD_ADDITIONAL_LINKS_EMPTY
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidUpdateShardRequests() {
        return new Object[][] {
            {
                new UpdateShardRequest(
                    INVALID_SHARD_NAME_TOO_LONG,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_SPACE,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    "",
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    INVALID_INHERITED_USERS_NULL,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_SHARD_FRIENDLY_NAME_TOO_LONG,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_SHARD_FRIENDLY_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    INVALID_SHARD_AVATAR_FILENAME_NON_UUID,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    INVALID_SHARD_AVATAR_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    INVALID_SHARD_BANNER_FILENAME_NON_UUID,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_GIF,
                    INVALID_SHARD_BANNER_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    INVALID_SHARD_DESCRIPTION_TOO_LONG,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    INVALID_INHERITED_SHARD_NAMES_NULL,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    INVALID_INHERITED_USERS_NULL,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    INVALID_SHARD_FEATURED_IMAGE_FILENAME_NON_UUID,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    INVALID_SHARD_FEATURED_IMAGE_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    INVALID_SHARD_FEATURED_IMAGE_LINK_TOO_LONG,
                    VALID_SHARD_RULES,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    INVALID_SHARD_RULES_RULE_NAME_TOO_LONG,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    INVALID_SHARD_RULES_RULE_DESCRIPTION_TOO_LONG,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    INVALID_SHARD_RULES_NULL,
                    VALID_SHARD_ADDITIONAL_LINKS
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    INVALID_SHARD_ADDITIONAL_LINKS_URL_TOO_LONG
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    INVALID_SHARD_ADDITIONAL_LINKS_TITLE_TOO_LONG
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK,
                    VALID_SHARD_RULES,
                    INVALID_SHARD_ADDITIONAL_LINKS_NULL
                )
            }
        };
    }

    @Test(dataProvider = "provideValidUpdateShardRequests")
    public void testValidUpdateShardRequests(final UpdateShardRequest updateShardRequest) {
        testValidShardRequests(updateShardRequest);
    }

    @Test(dataProvider = "provideInvalidUpdateShardRequests")
    public void testInvalidUpdateShardRequests(final UpdateShardRequest updateShardRequest) {
        testInvalidShardRequests(updateShardRequest);
    }
}
