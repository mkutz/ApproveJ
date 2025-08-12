CREATE
    TABLE
        shopping_cart(
            id UUID,
            PRIMARY KEY(id)
        );

CREATE
    TABLE
        item(
            id UUID,
            article_id UUID,
            title TEXT,
            image_url TEXT,
            quantity SMALLINT,
            price_per_piece MONEY,
            shopping_cart_id UUID,
            PRIMARY KEY(id)
        );

CREATE
    TABLE
        article(
            id UUID,
            title TEXT,
            description TEXT,
            image_url TEXT,
            price_per_unit MONEY,
            PRIMARY KEY(id)
        );
