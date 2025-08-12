select
    ae1_0.id,
    ae1_0.description,
    ae1_0.image_url,
    ae1_0.price_per_unit,
    ae1_0.title
from
    article ae1_0
where
    ae1_0.id=?;

insert
into
    article
    (description, image_url, price_per_unit, title, id)
values
    (?, ?, ?, ?, ?);

select
    ae1_0.id,
    ae1_0.description,
    ae1_0.image_url,
    ae1_0.price_per_unit,
    ae1_0.title
from
    article ae1_0
where
    ae1_0.id=?
