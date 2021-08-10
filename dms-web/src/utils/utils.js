const getIndex = (obj, list) => {
    if(obj === undefined || obj === null) {
        return -1;
    }
    const fieldName = obj.name;
    list.forEach((item, index) => {
        if(item.name === fieldName) {
            return index;
        }
    });
    return -1;
}
export default getIndex;